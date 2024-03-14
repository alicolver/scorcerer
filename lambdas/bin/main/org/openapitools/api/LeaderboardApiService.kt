package org.openapitools.api

import org.openapitools.model.LeaderboardInner

interface LeaderboardApiService {

    /**
     * GET /leaderboard
     * Returns the leaderboard
     *
     * @param leagueId  (optional)
     * @return Successful response (status code 200)
     * @see LeaderboardApi#leaderboardGet
     */
    fun leaderboardGet(leagueId: kotlin.String?): List<LeaderboardInner>
}
