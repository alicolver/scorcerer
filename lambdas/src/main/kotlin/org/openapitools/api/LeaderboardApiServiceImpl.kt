package org.openapitools.api

import org.openapitools.model.LeaderboardInner
import org.springframework.stereotype.Service
@Service
class LeaderboardApiServiceImpl : LeaderboardApiService {

    override fun leaderboardGet(leagueId: kotlin.String?): List<LeaderboardInner> {
        TODO("Implement me")
    }
}
