package game.doppelkopf.adapter.graphql.core.call.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.domain.call.enums.CallType
import java.util.*

data class CallResponse(
    val id: UUID,
    val callType: CallType,
    val cardsPlayedBefore: Int,
    val description: String,
    val cu: CreatedUpdated
)