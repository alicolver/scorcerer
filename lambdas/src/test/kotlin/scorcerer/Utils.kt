package scorcerer

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.models.State
import scorcerer.server.db.tables.*
import java.time.OffsetDateTime

fun givenUserExists(id: String, firstName: String, familyName: String = "Name", fixedPoints: Int = 0, livePoints: Int = 0) {
    transaction {
        MemberTable.insert {
            it[this.id] = id
            it[this.firstName] = firstName
            it[this.familyName] = familyName
            it[this.fixedPoints] = fixedPoints
            it[this.livePoints] = livePoints
        }
    }
}

fun givenMatchExists(
    homeTeamId: String,
    awayTeamId: String,
    matchDatetime: OffsetDateTime = OffsetDateTime.now(),
    matchState: State = State.UPCOMING,
    matchDay: Int = 1,
): String {
    return (
        transaction {
            MatchTable.insert {
                it[this.homeTeamId] = homeTeamId.toInt()
                it[this.awayTeamId] = awayTeamId.toInt()
                it[this.datetime] = matchDatetime
                it[this.state] = matchState
                it[this.venue] = "Test Venue"
                it[this.matchDay] = matchDay
                it[this.round] = MatchRound.GROUP_STAGE
            }
        } get MatchTable.id
        ).toString()
}

fun givenTeamExists(teamName: String): String {
    return (
        transaction {
            TeamTable.insert {
                it[this.name] = teamName
                it[this.flagUri] = ""
            }
        } get TeamTable.id
        ).toString()
}

fun givenLeagueExists(leagueId: String, leagueName: String) {
    transaction {
        LeagueTable.insert {
            it[this.id] = leagueId
            it[this.name] = leagueName
        }
    }
}

fun givenPredictionExists(matchId: String, userId: String, homeScore: Int, awayScore: Int, points: Int? = null): String {
    return (
        transaction {
            PredictionTable.insert {
                it[this.memberId] = userId
                it[this.matchId] = matchId.toInt()
                it[this.homeScore] = homeScore
                it[this.awayScore] = awayScore
                it[this.points] = points
            }
        } get PredictionTable.id
        ).toString()
}

fun givenUserInLeague(userId: String, leagueId: String) {
    transaction {
        LeagueMembershipTable.insert {
            it[this.memberId] = userId
            it[this.leagueId] = leagueId
        }
    }
}
