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
import org.openapitools.server.models.League
import org.postgresql.util.PSQLException
import scorcerer.server.db.tables.LeagueMembershipTable
import scorcerer.server.db.tables.LeagueTable
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
        val result = transaction {
            LeagueTable.selectAll().where { LeagueTable.id eq leagueId }
                .map { row ->
                    League(row[LeagueTable.id].toString(), row[LeagueTable.name])
                }
        }
        return result[0]
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
