package scorcerer.server.resources

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.apis.PredictionApi
import org.openapitools.server.models.CreatePrediction200Response
import org.openapitools.server.models.CreatePredictionRequest
import org.postgresql.util.PSQLException
import scorcerer.server.db.tables.PredictionTable
import scorcerer.utils.throwDatabaseError

class Prediction : PredictionApi() {
    override fun createPrediction(requesterUserId: String, createPredictionRequest: CreatePredictionRequest): CreatePrediction200Response {
        val id = try {
            transaction {
                PredictionTable.insert {
                    it[this.memberId] = requesterUserId
                    it[this.matchId] = createPredictionRequest.matchId.toInt()
                    it[this.homeScore] = createPredictionRequest.homeScore
                    it[this.awayScore] = createPredictionRequest.awayScore
                } get PredictionTable.id
            }
        } catch (e: PSQLException) {
            throwDatabaseError(e, "Prediction already exists, use updatePrediction instead")
        }

        return CreatePrediction200Response(id.toString())
    }
}
