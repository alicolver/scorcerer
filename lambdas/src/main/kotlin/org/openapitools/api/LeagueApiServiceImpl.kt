package org.openapitools.api

import org.openapitools.model.League
import org.openapitools.model.LeaguePost200Response
import org.openapitools.model.LeaguePostRequest
import org.springframework.stereotype.Service
@Service
class LeagueApiServiceImpl : LeagueApiService {

    override fun leagueLeagueIdGet(leagueId: kotlin.String): League {
        TODO("Implement me")
    }

    override fun leagueLeagueIdJoinPost(leagueId: kotlin.String): Unit {
        TODO("Implement me")
    }

    override fun leagueLeagueIdLeavePost(leagueId: kotlin.String): Unit {
        TODO("Implement me")
    }

    override fun leaguePost(leaguePostRequest: LeaguePostRequest): LeaguePost200Response {
        TODO("Implement me")
    }
}
