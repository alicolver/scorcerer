package scorcerer.server.db.tables

import org.ktorm.schema.Table
import org.ktorm.schema.*

/*
create table league_membership(
    id serial primary key,
    member_id varchar(30) REFERENCES member not null,
    league_id int REFERENCES league not null
);
 */

object LeagueMembershipTable : Table<Nothing>("league") {
    val id = int("id").primaryKey()
    val memberId = varchar("member_id")
    val leagueId = varchar("league_id")
}
