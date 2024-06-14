package scorcerer.server.schedule

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.openapitools.server.models.State
import scorcerer.server.db.Database
import scorcerer.server.db.tables.MatchTable
import scorcerer.server.log
import java.time.Clock
import java.time.OffsetDateTime

// Entrypoint for match starter lambda
class MatchStarter : RequestHandler<Unit, Unit> {
    init {
        Database.connectAndGenerateTables()
    }

    override fun handleRequest(input: Unit?, context: Context?) {
        log.info("Checking for games which have started")

        val clock = Clock.systemDefaultZone()
        // Add 1 minute to avoid missing any games which might be just starting
        val now = OffsetDateTime.now(clock).plusMinutes(1)
        log.info("Using now - $now")

        transaction {
            val matchesWhichHaveStarted = MatchTable
                .selectAll()
                .where((MatchTable.datetime less now) and (MatchTable.state eq State.UPCOMING))

            log.info("Found ${matchesWhichHaveStarted.count()} games which have already started")

            matchesWhichHaveStarted.forEach {
                log.info("Starting match ${it[MatchTable.id]}")
                MatchTable.update({ MatchTable.id eq it[MatchTable.id] }) {
                    it[state] = State.LIVE
                    it[homeScore] = 0
                    it[awayScore] = 0
                }
            }
        }
        log.info("All required matches started")
    }
}
