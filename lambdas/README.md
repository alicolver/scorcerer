# org.openapitools.server - Kotlin Server library for ScorePredictor

## Requires

* Kotlin 1.4.31
* Gradle 6.8.2

## Build

First, create the gradle wrapper script:

```
gradle wrapper
```

Then, run:

```
./gradlew check assemble
```

This runs all tests and packages the library.

## Features/Implementation Notes

* Supports JSON inputs/outputs, File inputs, and Form inputs.
* Supports collection formats for query parameters: csv, tsv, ssv, pipes.
* Some Kotlin and Java types are fully qualified to avoid conflicts with types defined in OpenAPI definitions.

<a id="documentation-for-api-endpoints"></a>
## Documentation for API Endpoints

All URIs are relative to *http://localhost*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*AuthApi* | [**authLoginPost**](docs/AuthApi.md#authloginpost) | **POST** /auth/login | 
*DefaultApi* | [**leaderboardGet**](docs/DefaultApi.md#leaderboardget) | **GET** /leaderboard | 
*LeagueApi* | [**leagueLeagueIdGet**](docs/LeagueApi.md#leagueleagueidget) | **GET** /league/{leagueId} | 
*LeagueApi* | [**leagueLeagueIdJoinPost**](docs/LeagueApi.md#leagueleagueidjoinpost) | **POST** /league/{leagueId}/join | 
*LeagueApi* | [**leagueLeagueIdLeavePost**](docs/LeagueApi.md#leagueleagueidleavepost) | **POST** /league/{leagueId}/leave | 
*LeagueApi* | [**leaguePost**](docs/LeagueApi.md#leaguepost) | **POST** /league | 
*MatchApi* | [**matchListGet**](docs/MatchApi.md#matchlistget) | **GET** /match/list | 
*MatchApi* | [**matchMatchIdPredictionsGet**](docs/MatchApi.md#matchmatchidpredictionsget) | **GET** /match/{matchId}/predictions | 
*MatchApi* | [**matchMatchIdScorePost**](docs/MatchApi.md#matchmatchidscorepost) | **POST** /match/{matchId}/score | 
*PredictionApi* | [**predictionPost**](docs/PredictionApi.md#predictionpost) | **POST** /prediction | 
*UserApi* | [**userPost**](docs/UserApi.md#userpost) | **POST** /user | 
*UserApi* | [**userUserIdPointsGet**](docs/UserApi.md#useruseridpointsget) | **GET** /user/{userId}/points | 
*UserApi* | [**userUserIdPredictionsGet**](docs/UserApi.md#useruseridpredictionsget) | **GET** /user/{userId}/predictions | 


<a id="documentation-for-models"></a>
## Documentation for Models

 - [org.openapitools.server.models.AuthLoginPostRequest](docs/AuthLoginPostRequest.md)
 - [org.openapitools.server.models.LeaderboardInner](docs/LeaderboardInner.md)
 - [org.openapitools.server.models.League](docs/League.md)
 - [org.openapitools.server.models.LeaguePost200Response](docs/LeaguePost200Response.md)
 - [org.openapitools.server.models.LeaguePostRequest](docs/LeaguePostRequest.md)
 - [org.openapitools.server.models.Match](docs/Match.md)
 - [org.openapitools.server.models.MatchMatchIdScorePostRequest](docs/MatchMatchIdScorePostRequest.md)
 - [org.openapitools.server.models.Prediction](docs/Prediction.md)
 - [org.openapitools.server.models.PredictionPost200Response](docs/PredictionPost200Response.md)
 - [org.openapitools.server.models.PredictionPostRequest](docs/PredictionPostRequest.md)
 - [org.openapitools.server.models.User](docs/User.md)
 - [org.openapitools.server.models.UserUserIdPointsGet200Response](docs/UserUserIdPointsGet200Response.md)


<a id="documentation-for-authorization"></a>
## Documentation for Authorization


Authentication schemes defined for the API:
<a id="bearerAuth"></a>
### bearerAuth

- **Type**: HTTP Bearer Token authentication (JWT)

