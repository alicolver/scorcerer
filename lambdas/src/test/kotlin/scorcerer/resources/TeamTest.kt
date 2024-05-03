package scorcerer.resources

import io.kotlintest.shouldBe
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.openapitools.server.models.CreateTeamRequest
import scorcerer.DatabaseTest
import scorcerer.server.db.tables.TeamTable
import scorcerer.server.resources.Team

class TeamTest : DatabaseTest() {
    @Test
    fun createTeam() {
        val id = Team().createTeam("test", CreateTeamRequest("England", "flag-uri"))
        id.teamId shouldBe "1"
    }

    @Test
    fun createMultipleTeams() {
        Team().createTeam("Luke", CreateTeamRequest("France", "flag-uri"))
        Team().createTeam("Ali", CreateTeamRequest("Germany", "flag-uri"))
        Team().createTeam("Simon", CreateTeamRequest("Spain", "flag-uri"))

        val numberOfTeams = transaction {
            TeamTable.selectAll().count()
        }

        numberOfTeams shouldBe 3
    }
}
