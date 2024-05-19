package scorcerer.server.events

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.openapitools.server.kotshiJson
import scorcerer.server.db.Database
import scorcerer.server.db.tables.MemberTable
import scorcerer.server.log
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class UserCreationEvent(
    val id: String,
    val firstName: String,
    val familyName: String,
)

class UserCreationEventHandler : RequestHandler<SQSEvent, Unit> {
    init {
        Database.connectAndGenerateTables()
    }
    override fun handleRequest(input: SQSEvent?, context: Context?) {
        log.info("Handling ${input?.records?.size} records")

        input?.records?.forEach {
            val userCreationEvent = kotshiJson.asA<UserCreationEvent>(it.body)

            transaction {
                MemberTable.insert { row ->
                    row[this.id] = userCreationEvent.id
                    row[this.name] = userCreationEvent.firstName
                    row[this.fixedPoints] = 0
                    row[this.livePoints] = 0
                }
            }
        }
    }
}
