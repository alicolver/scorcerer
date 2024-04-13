package scorcerer.server.db.tables

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

/*
create table match(
    id serial primary key,
    home_team varchar(30) not null,
    away_team varchar(30) not null
);
 */

object MatchTable : Table<Nothing>("match") {
    val id = int("id").primaryKey()
    val homeTeam = varchar("home_team")
    val awayTeam = varchar("away_team")
}
