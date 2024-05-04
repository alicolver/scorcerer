package scorcerer.server.resources

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.apis.LeagueApi
import org.openapitools.server.models.CreateLeague200Response
import org.openapitools.server.models.CreateLeagueRequest
import org.openapitools.server.models.LeaderboardInner
import org.openapitools.server.models.League
import org.openapitools.server.models.User
import org.postgresql.util.PSQLException
import scorcerer.server.db.tables.LeagueMembershipTable
import scorcerer.server.db.tables.LeagueTable
import scorcerer.server.db.tables.MemberTable
import scorcerer.utils.throwDatabaseError

class League : LeagueApi() {
    override fun createLeague(
        requesterUserId: String,
        createLeagueRequest: CreateLeagueRequest,
    ): CreateLeague200Response {
        val id = try {
            transaction {
                LeagueTable.insert {
                    it[this.name] = createLeagueRequest.leagueName
                    it[this.id] = createLeagueRequest.leagueName.lowercase().replace(" ", "-")
                } get LeagueTable.id
            }
        } catch (e: PSQLException) {
            throwDatabaseError(e, "League already exists")
        }

        // Add the creating user as a member of the new league
        transaction {
            LeagueMembershipTable.insert {
                it[this.memberId] = requesterUserId
                it[this.leagueId] = id
            }
        }

        return CreateLeague200Response(id)
    }

    override fun getLeague(requesterUserId: String, leagueId: String): League {
        val leagueName = transaction {
            LeagueTable.selectAll().where { LeagueTable.id eq leagueId }
                .map { row ->
                    row[LeagueTable.name]
                }
        }[0]
        val userIds = transaction {
            LeagueMembershipTable.selectAll().where { LeagueMembershipTable.leagueId eq leagueId }
                .map { row -> row[LeagueMembershipTable.memberId] }
        }
        val users = transaction {
            MemberTable.selectAll().where {
                MemberTable.id inList userIds
            }.map { row ->
                User(
                    row[MemberTable.name],
                    row[MemberTable.id],
                    row[MemberTable.fixedPoints],
                    row[MemberTable.livePoints],
                )
            }
        }
        return League(leagueId, leagueName, users)
    }

    override fun getLeagueLeaderboard(requesterUserId: String, leagueId: String): List<LeaderboardInner> {
        val users = transaction {
            LeagueMembershipTable.selectAll().where {
                LeagueMembershipTable.leagueId eq leagueId
            }.flatMap { row ->
                MemberTable.selectAll().where {
                    MemberTable.id eq row[LeagueMembershipTable.memberId]
                }.map { memberRow ->
                    User(
                        memberRow[MemberTable.name],
                        memberRow[MemberTable.id],
                        memberRow[MemberTable.fixedPoints],
                        memberRow[MemberTable.livePoints],
                    )
                }
            }
        }

        val sortedUsers = users.sortedWith(compareByDescending<User> { it.livePoints + it.fixedPoints }.thenBy { it.name })

        var currentPosition = 0
        var previousPoints = Int.MAX_VALUE
        val leaderboard = sortedUsers.mapIndexed { index, user ->
            if (user.livePoints + user.fixedPoints < previousPoints) {
                currentPosition = index + 1
            }
            previousPoints = user.livePoints + user.fixedPoints
            LeaderboardInner(currentPosition, user)
        }
        return leaderboard
    }

    override fun joinLeague(requesterUserId: String, leagueId: String) {
        transaction {
            LeagueMembershipTable.insert {
                it[this.memberId] = requesterUserId
                it[this.leagueId] = leagueId
            }
        }
    }

    override fun leaveLeague(requesterUserId: String, leagueId: String) {
        transaction {
            LeagueMembershipTable.deleteWhere {
                (LeagueMembershipTable.leagueId eq leagueId).and(LeagueMembershipTable.memberId eq requesterUserId)
            }
        }
    }
}
