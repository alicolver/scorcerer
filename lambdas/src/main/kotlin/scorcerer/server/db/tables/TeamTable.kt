package scorcerer.server.db.tables

import org.jetbrains.exposed.sql.Table

object TeamTable : Table("team") {
    val id = integer("id").uniqueIndex().autoIncrement()
    val name = varchar("name", 30).uniqueIndex()
    val flagUri = varchar("flag_uri", 60)
}
