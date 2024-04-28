package scorcerer.server.db.tables

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

/*
create table team(
    id serial primary key,
    name varchar(30) not null,
    flag_uri varchar(60) not null
);
 */

object TeamTable : Table<Nothing>("team") {
    val id = int("id").primaryKey()
    val name = varchar("name")
    val flagUri = int("flag_uri")
}
