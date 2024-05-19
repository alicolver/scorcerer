package scorcerer.server.db.tables

import org.jetbrains.exposed.sql.Table

object MemberTable : Table("member") {
    val id = varchar("id", 40).uniqueIndex()
    val firstName = varchar("firstName", 30)
    val familyName = varchar("familyName", 30)
    val fixedPoints = integer("fixed_points")
    val livePoints = integer("live_points")
}
