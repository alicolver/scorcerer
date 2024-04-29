package scorcerer.server.resources

import org.http4k.core.Response
import org.http4k.core.Status
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.apis.TeamApi
import org.openapitools.server.models.CreateTeam200Response
import org.openapitools.server.models.CreateTeamRequest
import org.postgresql.util.PSQLException
import scorcerer.server.ApiResponseError
import scorcerer.server.db.tables.TeamTable

class Team : TeamApi() {
    override fun createTeam(requesterUserId: String, createTeamRequest: CreateTeamRequest): CreateTeam200Response {
        val id = try {
            transaction {
                TeamTable.insert {
                    it[this.name] = createTeamRequest.teamName
                    it[this.flagUri] = createTeamRequest.flagUri
                } get TeamTable.id
            }
        } catch (e: PSQLException) {
            throw ApiResponseError(Response(Status.INTERNAL_SERVER_ERROR).body("Database error"))
        }
        return CreateTeam200Response(id.toString())
    }
}
