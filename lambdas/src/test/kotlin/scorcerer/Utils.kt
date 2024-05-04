package scorcerer

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import scorcerer.server.db.tables.MemberTable

fun givenUserExists(id: String, name: String, fixedPoints: Int, livePoints: Int) {
    transaction {
        MemberTable.insert {
            it[this.id] = id
            it[this.name] = name
            it[this.fixedPoints] = fixedPoints
            it[this.livePoints] = livePoints
        }
    }
}
