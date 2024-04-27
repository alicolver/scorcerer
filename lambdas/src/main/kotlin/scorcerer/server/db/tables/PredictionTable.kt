package scorcerer.server.db.tables

import org.ktorm.dsl.isNull
import org.ktorm.schema.*

/*
CREATE TYPE result_enum AS ENUM ('HOME', 'AWAY');
create table prediction(
    id serial primary key,
    member_id varchar(30) not null REFERENCES member,
    match_id integer not null REFERENCES match,
    home_score integer CHECK (home_score >= 0) not null,
    away_score integer CHECK (away_score >= 0) not null,
    result result_enum
    unique (match_id, user_id)
);
 */

object PredictionTable : Table<Nothing>("prediction") {
    val id = int("id").primaryKey()
    val memberId = int("member_id")
    val matchId = int("match_id")
    val homeScore = int("home_score")
    val awayScore = int("away_score")
    val result = enum<MatchResult>("result").isNull()
}
