package scorcerer.server.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import scorcerer.server.Environment
import scorcerer.server.db.tables.*

fun main() {
    Database.connect(
        "jdbc:postgresql://${Environment.DatabaseUrl}:${Environment.DatabasePort}/${Environment.DatabaseName}",
        driver = "org.h2.Driver",
        user = Environment.DatabaseUser,
        password = Environment.DatabasePassword,
    )

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(UsersTable, MatchTable, PredictionTable, TeamTable, LeagueTable, LeagueMembershipTable)
    }
}
