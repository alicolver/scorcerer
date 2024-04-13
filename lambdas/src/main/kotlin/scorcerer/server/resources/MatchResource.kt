package scorcerer.server.resources

import org.openapitools.server.apis.MatchApi
import org.openapitools.server.models.Match
import org.openapitools.server.models.Prediction
import org.openapitools.server.models.SetMatchScoreRequest

class MatchResource : MatchApi() {
    override fun getMatchPredictions(requesterUserId: String, matchId: String, leagueId: String?): List<Prediction> {
        TODO("Not yet implemented")
    }

    override fun listMatches(requesterUserId: String, filterType: String?): List<Match> {
        return listOf(
            Match("England", "France", "12345"),
            Match("Scotland", "Germany", "12346"),
        )
    }

    override fun setMatchScore(requesterUserId: String, matchId: String, setMatchScoreRequest: SetMatchScoreRequest) {
        TODO("Not yet implemented")
    }
}
