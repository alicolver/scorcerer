package scorcerer.server.resources

import org.http4k.core.Response
import org.http4k.core.Status
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.apis.UserApi
import org.openapitools.server.models.GetUserPoints200Response
import org.openapitools.server.models.Prediction
import org.openapitools.server.models.SignupRequest
import scorcerer.server.ApiResponseError
import scorcerer.server.db.tables.MemberTable

class User : UserApi() {
    override fun getUserPoints(requesterUserId: String, userId: String): GetUserPoints200Response {
        return transaction {
            MemberTable
                .selectAll()
                .where { MemberTable.id eq userId }
                .firstOrNull()
                ?.let { row -> GetUserPoints200Response(row[MemberTable.livePoints], row[MemberTable.fixedPoints]) }
                ?: throw ApiResponseError(Response(Status.BAD_REQUEST).body("User does not exist"))
        }
    }

    override fun getUserPredictions(requesterUserId: String, userId: String, leagueId: String?): List<Prediction> {
        TODO("Not yet implemented")
    }

    override fun signup(signupRequest: SignupRequest): Unit = transaction {
        MemberTable.insert {
            it[this.id] = "id-from-cognito"
            it[this.name] = signupRequest.name
            it[this.fixedPoints] = 0
            it[this.livePoints] = 0
        }
    }
}
