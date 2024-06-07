package scorcerer.server.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestampWithTimeZone

enum class MatchState(val value: String) {
    LIVE("LIVE"),
    UPCOMING("UPCOMING"),
    COMPLETED("COMPLETED"),
}

enum class MatchResult(val value: String) {
    HOME("HOME"),
    AWAY("AWAY"),
}

enum class MatchRound(val value: String) {
    GROUP_STAGE("GROUP_STAGE"),
    ROUND_OF_SIXTEEN("ROUND_OF_SIXTEEN"),
    QUARTER_FINAL("QUARTER_FINAL"),
    SEMI_FINAL("SEMI_FINAL"),
    FINAL("FINAL"),
}

object MatchTable : Table("match") {
    val id = integer("id").uniqueIndex().autoIncrement()
    val homeTeamId = integer("home_team_id").references(TeamTable.id)
    val awayTeamId = integer("away_team_id").references(TeamTable.id)
    val datetime = timestampWithTimeZone("datetime")
    val homeScore = integer("home_score").nullable()
    val awayScore = integer("away_score").nullable()
    val result = enumerationByName<MatchResult>("result", 10).nullable()
    val state = enumerationByName<MatchState>("state", 10)
    val venue = varchar("venue", 30)
    val matchDay = integer("match_day").check { it.greaterEq(1) }
    val round = enumerationByName<MatchRound>("round", 15)
}
