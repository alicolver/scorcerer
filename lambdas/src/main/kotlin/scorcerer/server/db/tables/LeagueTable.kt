package scorcerer.server.db.tables

import org.jetbrains.exposed.sql.Table

object LeagueTable : Table("league") {
    val id = varchar("id", 30).uniqueIndex()
    val name = varchar("name", 30)
}
