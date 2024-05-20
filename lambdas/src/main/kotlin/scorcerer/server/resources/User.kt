package scorcerer.server.resources

import aws.sdk.kotlin.services.cognitoidentityprovider.CognitoIdentityProviderClient
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AdminCreateUserRequest
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AttributeType
import aws.sdk.kotlin.services.cognitoidentityprovider.model.MessageActionType
import aws.sdk.kotlin.services.sqs.SqsClient
import aws.sdk.kotlin.services.sqs.model.SendMessageRequest
import kotlinx.coroutines.runBlocking
import org.http4k.core.RequestContexts
import org.http4k.core.Response
import org.http4k.core.Status
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.apis.UserApi
import org.openapitools.server.models.GetUserPoints200Response
import org.openapitools.server.models.League
import org.openapitools.server.models.Prediction
import org.openapitools.server.models.SignupRequest
import org.openapitools.server.models.User
import org.openapitools.server.toJson
import scorcerer.server.ApiResponseError
import scorcerer.server.Environment
import scorcerer.server.db.tables.LeagueMembershipTable
import scorcerer.server.db.tables.LeagueTable
import scorcerer.server.db.tables.MemberTable
import scorcerer.server.db.tables.PredictionTable
import scorcerer.server.events.UserCreationEvent
import scorcerer.server.log

class User(context: RequestContexts) : UserApi(context) {
    private val cognitoClient = CognitoIdentityProviderClient { region = "eu-west-2" }
    private val sqsClient = SqsClient { region = "eu-west-2" }

    override fun getUserLeagues(requesterUserId: String, userId: String): List<League> {
        return transaction {
            val userLeagueIds = LeagueMembershipTable
                .select(LeagueMembershipTable.leagueId).where { LeagueMembershipTable.memberId eq userId }
                .map { it[LeagueMembershipTable.leagueId] }

            val leaguesWithUsers = (LeagueTable innerJoin LeagueMembershipTable innerJoin MemberTable)
                .selectAll().where { LeagueTable.id inList userLeagueIds }
                .groupBy { it[LeagueTable.id] }
                .mapValues { entry ->
                    val leagueId = entry.key
                    val rows = entry.value

                    val leagueName = rows.first()[LeagueTable.name]
                    val usersInLeague = rows.map {
                        User(
                            it[MemberTable.firstName],
                            it[MemberTable.familyName],
                            it[MemberTable.id],
                            it[MemberTable.fixedPoints],
                            it[MemberTable.livePoints],
                        )
                    }

                    League(leagueId, leagueName, usersInLeague)
                }
            leaguesWithUsers.values.toList()
        }
    }

    override fun getUserPoints(requesterUserId: String, userId: String): GetUserPoints200Response {
        return transaction {
            MemberTable
                .selectAll()
                .where { MemberTable.id eq userId }
                .firstOrNull()
                ?.let { row -> GetUserPoints200Response(row[MemberTable.fixedPoints], row[MemberTable.livePoints]) }
                ?: throw ApiResponseError(Response(Status.BAD_REQUEST).body("User does not exist"))
        }
    }

    override fun getUserPredictions(requesterUserId: String, userId: String): List<Prediction> {
        return transaction {
            PredictionTable.selectAll().where { (PredictionTable.memberId eq userId) }.map { row ->
                Prediction(
                    row[PredictionTable.homeScore],
                    row[PredictionTable.awayScore],
                    row[PredictionTable.matchId].toString(),
                    row[PredictionTable.id].toString(),
                    row[PredictionTable.memberId],
                    row[PredictionTable.points],
                )
            }
        }
    }

    override fun signup(signupRequest: SignupRequest) {
        val request = AdminCreateUserRequest {
            username = signupRequest.email
            userPoolId = Environment.CognitoUserPoolId
            messageAction = MessageActionType.Suppress
            userAttributes = listOf(
                AttributeType {
                    name = "email"
                    value = signupRequest.email
                },
                AttributeType {
                    name = "given_name"
                    value = signupRequest.firstName
                },
                AttributeType {
                    name = "family_name"
                    value = signupRequest.familyName
                },
            )
        }

        val passwordRequest = AdminSetUserPasswordRequest {
            password = signupRequest.password
            username = signupRequest.email
            userPoolId = Environment.CognitoUserPoolId
            permanent = true
        }

        val userId = runBlocking {
            val response = cognitoClient.adminCreateUser(request)
            cognitoClient.adminSetUserPassword(passwordRequest)
            response.user?.attributes?.find { it.name == "sub" }?.value ?: throw Exception("Failed to find user sub")
        }

        log.info("Created user ($userId) and set password successfully")

        runBlocking {
            sqsClient.sendMessage(
                SendMessageRequest {
                    queueUrl = Environment.UserCreationQueueUrl
                    messageBody = UserCreationEvent(userId, signupRequest.firstName, signupRequest.familyName).toJson()
                },
            )
        }
    }
}
