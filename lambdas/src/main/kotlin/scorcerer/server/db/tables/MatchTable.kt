package scorcerer.server.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object MatchTable : Table("match") {
    val id = integer("id").index()
    val homeTeamId = integer("home_team_id").references(TeamTable.id)
    val awayTeamId = integer("away_team_id").references(TeamTable.id)
    val datetime = timestamp("datetime")
    val homeScore = integer("home_score")
    val awayScore = integer("away_score")
    val result = varchar("result", 10).nullable()
    val state = varchar("state", 10)
    val venue = varchar("venue", 20)
    val matchDay = integer("match_day")
}
