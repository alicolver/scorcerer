package scorcerer.server.db.tables

import org.jetbrains.exposed.sql.Table
import scorcerer.server.db.tables.LeagueMembershipTable.references

object PredictionTable : Table("prediction") {
    val id = integer("id").uniqueIndex().autoIncrement()
    val memberId = varchar("member_id", 30).references(MemberTable.id)
    val matchId = integer("match_id").references(MatchTable.id)
    val homeScore = integer("home_score").check { it.greaterEq(0) }
    val awayScore = integer("away_score").check { it.greaterEq(0) }
    val result = enumeration<MatchResult>("result").nullable()
}
