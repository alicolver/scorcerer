package scorcerer.server.db.tables

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

/*
create table member(
    id varchar(30) primary key,
    name varchar(30) not null,
    flag_uri varchar(60) not null,
    fixed_points integer CHECK (fixed_points >= 0) not null,
    live_points integer CHECK (live_points >= 0) not null
);
 */

object MemberTable : Table<Nothing>("user") {
    val id = varchar("id")
    val name = varchar("name")
    val fixedPoints = int("fixed_points")
    val livePoints = int("live_points")
}
