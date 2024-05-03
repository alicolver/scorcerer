package scorcerer.server.resources

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.apis.TeamApi
import org.openapitools.server.models.CreateTeam200Response
import org.openapitools.server.models.CreateTeamRequest
import scorcerer.server.db.tables.TeamTable

class Team() : TeamApi() {
    override fun createTeam(requesterUserId: String, createTeamRequest: CreateTeamRequest): CreateTeam200Response {
        val id = transaction {
            TeamTable.insert {
                it[this.name] = createTeamRequest.teamName
                it[this.flagUri] = createTeamRequest.flagUri
            } get TeamTable.id
        }

        return CreateTeam200Response(id.toString())
    }
}
