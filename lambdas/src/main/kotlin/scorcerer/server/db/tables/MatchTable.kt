package scorcerer.server.db.tables

import org.ktorm.schema.*

/*
create table match(
    id serial primary key,
    home_team_id varchar(30) not null,
    away_team_id varchar(30) not null,
    datetime datetime() not null,
    home_score int() not null,
    away_score int() not null,
    result varchar(10),
    state varchar(10),
    venue varchar(20) not null,
    matchDay int() not null
);
 */

object MatchTable : Table<Nothing>("match") {
    val id = int("id").primaryKey()
    val homeTeamId = varchar("home_team_id")
    val awayTeamId = varchar("away_team_id")
    val datetime = datetime("datetime")
    val homeScore = int("home_score")
    val awayScore = int("away_socre")
    val result = varchar("result")
    val state = varchar("state")
    val venue = varchar("venue")
    val matchDay = int("match_day")

}
