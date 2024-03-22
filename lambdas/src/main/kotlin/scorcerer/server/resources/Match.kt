package scorcerer.server.resources

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import org.openapitools.server.apis.MatchApi
import org.openapitools.server.models.Match
import org.openapitools.server.models.MatchMatchIdScorePostRequest
import org.openapitools.server.models.Prediction

@Path("/")
class Match : MatchApi{
    override fun matchListGet(filterType: String?): List<Match> {
        TODO("Not yet implemented")
    }

    override fun matchMatchIdPredictionsGet(matchId: String, leagueId: String?): List<Prediction> {
        TODO("Not yet implemented")
    }

    override fun matchMatchIdScorePost(matchId: String, matchMatchIdScorePostRequest: MatchMatchIdScorePostRequest) {
        TODO("Not yet implemented")
    }
}