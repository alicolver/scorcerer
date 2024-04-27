package scorcerer.server.db.tables

import org.ktorm.dsl.isNull
import org.ktorm.schema.*

/*
CREATE TYPE result_enum AS ENUM ('HOME', 'AWAY');
CREATE TYPE state_enum AS ENUM ('UPCOMING', 'LIVE', 'COMPLETED');
create table match(
  id serial primary key,
  home_team_id int not null REFERENCES team,
  away_team_id int not null REFERENCES team,
  datetime timestamp not null,
  home_score integer not null,
  away_score integer not null,
  result result_enum,
  state state_enum not null,
  venue varchar(20) not null,
  matchDay integer not null
);
 */

enum class MatchState(val value: String) {
    LIVE("LIVE"),
    UPCOMING("UPCOMING"),
    COMPLETED("COMPLETED")
}

enum class MatchResult(val value: String) {
    HOME("HOME"),
    AWAY("AWAY")
}

object MatchTable : Table<Nothing>("match") {
    val id = int("id").primaryKey()
    val homeTeamId = varchar("home_team_id")
    val awayTeamId = varchar("away_team_id")
    val datetime = timestamp("datetime")
    val homeScore = int("home_score")
    val awayScore = int("away_socre")
    val result = enum<MatchResult>("result").isNull()
    val state = enum<MatchState>("state")
    val venue = varchar("venue")
    val matchDay = int("match_day")
}
