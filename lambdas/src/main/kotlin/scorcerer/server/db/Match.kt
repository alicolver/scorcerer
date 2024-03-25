package scorcerer.server.db

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object Match: IntIdTable() {
    val homeTeamName: Column<String> = varchar("home_team_name", 50)
    val awayTeamName: Column<String> = varchar("away_team_name", 50)
}