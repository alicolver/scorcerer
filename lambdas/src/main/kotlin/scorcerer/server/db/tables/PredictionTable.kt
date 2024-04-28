package scorcerer.server.db.tables

import org.jetbrains.exposed.sql.Table

object PredictionTable : Table("prediction") {
    val id = integer("id").index()
    val userId = varchar("user_id", 30).references(UsersTable.id)
    val matchId = integer("match_id").references(MatchTable.id)
    val homeScore = integer("home_score")
    val awayScore = integer("away_score")
    val result = varchar("result", 10).nullable()
}
