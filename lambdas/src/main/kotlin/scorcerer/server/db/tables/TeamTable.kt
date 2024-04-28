package scorcerer.server.db.tables

import org.jetbrains.exposed.sql.Table

object TeamTable : Table("team") {
    val id = integer("id").index()
    val name = varchar("name", 20)
    val flagUri = varchar("flag_uri", 50)
}
