package scorcerer

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import scorcerer.server.db.Database as ServerDatabase

open class DatabaseTest {
    init {
        Database.connect(
            "jdbc:h2:mem:test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=1",
            driver = "org.h2.Driver",
        )
    }

    @BeforeEach
    fun resetDatabases() = transaction {
        ServerDatabase.dropTables()
        ServerDatabase.generateTables()
    }
}
