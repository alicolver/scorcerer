package scorcerer.server.db.tables

import org.jetbrains.exposed.sql.Table

object LeagueMembershipTable : Table("league_membership") {
    val id = integer("id").uniqueIndex().autoIncrement()
    val memberId = varchar("member_id", 40).references(MemberTable.id)
    val leagueId = varchar("league_id", 30).references(LeagueTable.id)
}
