package scorcerer.server.db.tables

import org.jetbrains.exposed.sql.Table

object UsersTable : Table("users") {
    val id = varchar("id", 30).index()
    val name = varchar("name", 30)
    val fixedPoints = integer("fixed_points")
    val livePoints = integer("live_points")
}
