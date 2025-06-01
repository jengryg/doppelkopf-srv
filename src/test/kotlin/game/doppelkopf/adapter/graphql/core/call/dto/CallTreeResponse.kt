package game.doppelkopf.adapter.graphql.core.call.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.adapter.graphql.core.UuidResponse
import game.doppelkopf.domain.call.enums.CallType
import java.util.*

data class CallTreeResponse(
    val id: UUID,
    val callType: CallType,
    val cardsPlayedBefore: Int,
    val description: String,
    val cu: CreatedUpdated,
    val hand: UuidResponse,
    val round: UuidResponse,
)
