package scorcerer.server.db.tables

import org.ktorm.schema.Table
import org.ktorm.schema.int

/*
create table prediction(
    id serial primary key,
    match_id integer REFERENCES match,
    home_score integer CHECK (home_score >= 0) not null,
    away_score integer CHECK (away_score >= 0) not null
);
 */

object PredictionTable : Table<Nothing>("prediction") {
    val id = int("id").primaryKey()
    val matchId = int("match_id")
    val homeScore = int("home_score")
    val awayScore = int("away_score")
}
