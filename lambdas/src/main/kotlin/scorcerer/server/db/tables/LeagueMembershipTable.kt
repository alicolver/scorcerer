package scorcerer.server.db.tables

import org.jetbrains.exposed.sql.Table

object LeagueMembershipTable : Table("league") {
    val id = integer("id").index()
    val userId = varchar("user_id", 30).references(UsersTable.id)
    val leagueId = integer("league_id").references(LeagueTable.id)
}
