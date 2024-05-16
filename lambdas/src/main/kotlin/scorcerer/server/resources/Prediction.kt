package scorcerer.server.resources

import org.http4k.core.Response
import org.http4k.core.Status
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.openapitools.server.apis.PredictionApi
import org.openapitools.server.models.CreatePrediction200Response
import org.openapitools.server.models.CreatePredictionRequest
import scorcerer.server.ApiResponseError
import scorcerer.server.db.tables.MatchResult
import scorcerer.server.db.tables.MatchTable
import scorcerer.server.db.tables.PredictionTable
import java.time.OffsetDateTime

class Prediction : PredictionApi() {
    override fun createPrediction(
        requesterUserId: String,
        createPredictionRequest: CreatePredictionRequest,
    ): CreatePrediction200Response {
        val matchDatetime = transaction {
            MatchTable.selectAll().where {
                MatchTable.id eq createPredictionRequest.matchId.toInt()
            }.firstOrNull()?.let { row ->
                row[MatchTable.datetime]
            } ?: throw ApiResponseError(Response(Status.BAD_REQUEST).body("Match does not exist"))
        }
        if (matchDatetime.isBefore(OffsetDateTime.now())) {
            throw ApiResponseError(Response(Status.BAD_REQUEST).body("Match is not in future"))
        }
        var predictionId = transaction {
            PredictionTable.selectAll()
                .where { (PredictionTable.memberId eq requesterUserId).and(PredictionTable.matchId eq createPredictionRequest.matchId.toInt()) }
                .firstOrNull()?.let { row ->
                    row[PredictionTable.id]
                }
        }

        predictionId?.let { id ->
            transaction {
                PredictionTable.update({ PredictionTable.id eq id }) {
                    it[homeScore] = createPredictionRequest.homeScore
                    it[awayScore] = createPredictionRequest.awayScore
                    it[result] = if (createPredictionRequest.toGoThrough != null) {
                        MatchResult.valueOf(createPredictionRequest.toGoThrough.value)
                    } else {
                        null
                    }
                }
            }
        } ?: run {
            predictionId = transaction {
                PredictionTable.insert {
                    it[this.memberId] = requesterUserId
                    it[this.matchId] = createPredictionRequest.matchId.toInt()
                    it[this.homeScore] = createPredictionRequest.homeScore
                    it[this.awayScore] = createPredictionRequest.awayScore
                } get PredictionTable.id
            }
        }

        return CreatePrediction200Response(predictionId.toString())
    }
}
