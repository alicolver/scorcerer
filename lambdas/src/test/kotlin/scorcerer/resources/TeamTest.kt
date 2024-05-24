package scorcerer.resources

import io.kotlintest.shouldBe
import org.http4k.core.RequestContexts
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.openapitools.server.models.CreateTeamRequest
import scorcerer.DatabaseTest
import scorcerer.givenTeamExists
import scorcerer.server.ApiResponseError
import scorcerer.server.db.tables.TeamTable
import scorcerer.server.resources.Team

class TeamTest : DatabaseTest() {
    @Test
    fun createTeam() {
        val id = Team(RequestContexts()).createTeam("test", CreateTeamRequest("England", "flag-uri"))
        id.teamId shouldBe "1"
    }

    @Test
    fun createMultipleTeams() {
        Team(RequestContexts()).createTeam("Luke", CreateTeamRequest("France", "flag-uri"))
        Team(RequestContexts()).createTeam("Ali", CreateTeamRequest("Germany", "flag-uri"))
        Team(RequestContexts()).createTeam("Simon", CreateTeamRequest("Spain", "flag-uri"))

        val numberOfTeams = transaction {
            TeamTable.selectAll().count()
        }

        numberOfTeams shouldBe 3
    }

    @Test
    fun getTeamWhenTeamExists() {
        val teamId = givenTeamExists("scotland")
        Team(RequestContexts()).getTeam("userId", teamId).teamName shouldBe "Scotland"
    }

    @Test
    fun getTeamWhenTeamDoesNotExistsRaises() {
        assertThrows<ApiResponseError> {
            Team(RequestContexts()).getTeam("", "1")
        }
    }

    @Test
    fun getTeamByNAmeWhenTeamExists() {
        val teamId = givenTeamExists("scotland")
        Team(RequestContexts()).getTeamByName("userId", "Scotland").teamId shouldBe teamId
    }

    @Test
    fun getTeamByNameWhenTeamDoesNotExistRaises() {
        assertThrows<ApiResponseError> {
            Team(RequestContexts()).getTeamByName("", "teamName")
        }
    }
}
