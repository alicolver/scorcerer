package scorcerer.server.resources

import org.http4k.core.RequestContexts
import org.http4k.core.Response
import org.http4k.core.Status
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.apis.TeamApi
import org.openapitools.server.models.CreateTeam200Response
import org.openapitools.server.models.CreateTeamRequest
import org.openapitools.server.models.Team
import scorcerer.server.ApiResponseError
import scorcerer.server.db.tables.TeamTable

class Team(context: RequestContexts) : TeamApi(context) {
    override fun createTeam(requesterUserId: String, createTeamRequest: CreateTeamRequest): CreateTeam200Response {
        val id = transaction {
            TeamTable.insert {
                it[this.name] = createTeamRequest.teamName
                it[this.flagUri] = createTeamRequest.flagUri
            } get TeamTable.id
        }
        return CreateTeam200Response(id.toString())
    }

    override fun getTeam(requesterUserId: String, teamId: String): Team {
        val team = transaction {
            TeamTable.selectAll().where { TeamTable.id eq teamId.toInt() }.firstOrNull()
                ?.let { row ->
                    Team(row[TeamTable.id].toString(), row[TeamTable.name], row[TeamTable.flagUri])
                } ?: throw ApiResponseError(Response(Status.BAD_REQUEST).body("Team does not exist"))
        }
        return team
    }
}
