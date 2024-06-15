package scorcerer.server

object Environment {
    private const val DEFAULT_DATABASE_PORT = "5432"

    val DatabaseUser = getEnvVarOrFail("DB_USER")
    val DatabasePassword = getEnvVarOrFail("DB_PASSWORD")
    val DatabaseUrl = getEnvVarOrFail("DB_URL")
    val DatabaseName = getEnvVarOrFail("DB_NAME")
    val DatabasePort: String = getEnvVarOrDefault("DB_PORT", DEFAULT_DATABASE_PORT)
    val LeaderboardBucketName: String = getEnvVarOrFail("LEADERBOARD_BUCKET_NAME")

    val CognitoUserPoolClientId = getEnvVarOrFail("USER_POOL_CLIENT_ID")
    val CognitoUserPoolId = getEnvVarOrFail("USER_POOL_ID")

    val UserCreationQueueUrl = getEnvVarOrFail("USER_CREATION_QUEUE_URL")
    val ScoreUpdateQueueUrl = getEnvVarOrFail("SCORE_UPDATE_QUEUE_URL")

    private fun getEnvVarOrFail(name: String) = System.getenv(name) ?: throw Exception("Expected environment variable $name to be set")

    private fun getEnvVarOrDefault(name: String, default: String) = System.getenv(name) ?: default
}
