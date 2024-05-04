package scorcerer

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import scorcerer.server.db.tables.LeagueMembershipTable
import scorcerer.server.db.tables.LeagueTable
import scorcerer.server.db.tables.MatchState
import scorcerer.server.db.tables.MatchTable
import scorcerer.server.db.tables.MemberTable
import scorcerer.server.db.tables.PredictionTable
import scorcerer.server.db.tables.TeamTable
import java.time.OffsetDateTime

fun givenUserExists(id: String, name: String, fixedPoints: Int = 0, livePoints: Int = 0) {
    transaction {
        MemberTable.insert {
            it[this.id] = id
            it[this.name] = name
            it[this.fixedPoints] = fixedPoints
            it[this.livePoints] = livePoints
        }
    }
}

fun givenMatchExists(homeTeamId: String, awayTeamId: String): String {
    return (
        transaction {
            MatchTable.insert {
                it[this.homeTeamId] = homeTeamId.toInt()
                it[this.awayTeamId] = awayTeamId.toInt()
                it[this.datetime] = OffsetDateTime.now()
                it[this.state] = MatchState.UPCOMING
                it[this.venue] = "Test Venue"
                it[this.matchDay] = 1
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

fun givenPredictionExists(matchId: String, userId: String, homeScore: Int, awayScore: Int): String {
    return (
        transaction {
            PredictionTable.insert {
                it[this.memberId] = userId
                it[this.matchId] = matchId.toInt()
                it[this.homeScore] = homeScore
                it[this.awayScore] = awayScore
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
