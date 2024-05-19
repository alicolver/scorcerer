package scorcerer.utils

import io.kotlintest.shouldBe
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import scorcerer.*
import scorcerer.server.db.tables.MatchState
import scorcerer.server.db.tables.MemberTable

class PointsTest : DatabaseTest() {
    @Test
    fun testRecalculateLivePoints() {
        givenUserExists("test1", "name")
        givenUserExists("test2", "name")
        givenUserExists("test3", "name")
        val homeTeamId = givenTeamExists("Scotland")
        val awayTeamId = givenTeamExists("England")
        val matchId = givenMatchExists(homeTeamId, awayTeamId, matchState = MatchState.LIVE)
        val anotherMatchId = givenMatchExists(homeTeamId, awayTeamId, matchState = MatchState.LIVE)
        val matchInPast = givenMatchExists(homeTeamId, awayTeamId, matchState = MatchState.COMPLETED)

        givenPredictionExists(matchId, "test1", 1, 1, 2)
        givenPredictionExists(matchId, "test2", 1, 1, 3)
        givenPredictionExists(matchId, "test3", 1, 1, 1)

        givenPredictionExists(anotherMatchId, "test1", 1, 1, 0)
        givenPredictionExists(anotherMatchId, "test2", 1, 1, 1)
        givenPredictionExists(anotherMatchId, "test3", 1, 1, 2)

        givenPredictionExists(matchInPast, "test1", 1, 1, 0)
        givenPredictionExists(matchInPast, "test2", 1, 1, 1)
        givenPredictionExists(matchInPast, "test3", 1, 1, 2)

        recalculateLivePoints()
        transaction {
            MemberTable.selectAll().where { MemberTable.id eq "test1" }.map { row ->
                row[MemberTable.livePoints] shouldBe 2
            }
            MemberTable.selectAll().where { MemberTable.id eq "test2" }.map { row ->
                row[MemberTable.livePoints] shouldBe 4
            }
            MemberTable.selectAll().where { MemberTable.id eq "test3" }.map { row ->
                row[MemberTable.livePoints] shouldBe 3
            }
        }
    }
}
