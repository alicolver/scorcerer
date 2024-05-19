package scorcerer.utils

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import scorcerer.server.db.tables.MatchState
import scorcerer.server.db.tables.MatchTable
import scorcerer.server.db.tables.MemberTable
import scorcerer.server.db.tables.PredictionTable

fun recalculateLivePoints() {
    transaction {
        val liveMatchIds = MatchTable.selectAll().where { MatchTable.state eq MatchState.LIVE }.map { it[MatchTable.id] }

        val livePointsByUser = PredictionTable
            .select(PredictionTable.memberId, PredictionTable.points.sum())
            .where { PredictionTable.matchId inList liveMatchIds }
            .groupBy(PredictionTable.memberId)
            .map { row ->
                row[PredictionTable.memberId] to (row[PredictionTable.points.sum()] ?: 0)
            }

        livePointsByUser.forEach { (userId, totalLivePoints) ->
            MemberTable.update({ MemberTable.id eq userId }) {
                it[livePoints] = totalLivePoints
            }
        }
    }
}
