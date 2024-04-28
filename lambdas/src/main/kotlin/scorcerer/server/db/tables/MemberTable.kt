package scorcerer.server.db.tables

import org.jetbrains.exposed.sql.Table

object MemberTable : Table("user") {
    val id = varchar("id", 30).uniqueIndex()
    val name = varchar("name", 30)
    val fixedPoints = integer("fixed_points")
    val livePoints = integer("live_points")
}
