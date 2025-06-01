package game.doppelkopf.adapter.graphql.core.turn.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.adapter.graphql.core.UuidResponse
import java.util.*

data class TurnTreeResponse(
    val id: UUID,
    val cu: CreatedUpdated,
    val number: Int,
    val card: String,
    val round: UuidResponse,
    val hand: UuidResponse,
    val trick: UuidResponse,
)
