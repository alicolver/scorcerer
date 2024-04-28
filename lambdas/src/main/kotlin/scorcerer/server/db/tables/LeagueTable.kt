package scorcerer.server.db.tables

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

/*
create table league(
    id serial primary key,
    name varchar(30) not null
);
 */

object LeagueTable : Table<Nothing>("league") {
    val id = int("id").primaryKey()
    val name = varchar("name")
}
