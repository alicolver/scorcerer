package org.openapitools.api

import org.openapitools.model.LeaderboardInner
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity

class LeaderboardApiTest {

    private val service: LeaderboardApiService = LeaderboardApiServiceImpl()
    private val api: LeaderboardApiController = LeaderboardApiController(service)

    /**
     * To test LeaderboardApiController.leaderboardGet
     *
     * @throws ApiException
     *          if the Api call fails
     */
    @Test
    fun leaderboardGetTest() {
        val leagueId: kotlin.String? = TODO()
        val response: ResponseEntity<List<LeaderboardInner>> = api.leaderboardGet(leagueId)

        // TODO: test validations
    }
}
