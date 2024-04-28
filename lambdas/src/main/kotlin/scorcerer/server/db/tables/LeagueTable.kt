package scorcerer.server.db.tables

import org.jetbrains.exposed.sql.Table

object LeagueTable : Table("league") {
    val id = integer("id").index()
    val name = varchar("name", 30)
}
