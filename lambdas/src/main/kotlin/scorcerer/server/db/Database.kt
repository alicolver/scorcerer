package scorcerer.server.db

import org.ktorm.database.Database
import scorcerer.server.Environment

class Database {
    val database: Database = Database.connect(
        "jdbc:postgresql://${Environment.DatabaseUrl}:${Environment.DatabasePort}/${Environment.DatabaseName}",
        user = Environment.DatabaseUser,
        password = Environment.DatabasePassword,
    )
}
