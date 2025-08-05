package game.doppelkopf.adapter.api.core.call.dto

import game.doppelkopf.adapter.persistence.model.call.CallEntity
import game.doppelkopf.domain.call.enums.CallType
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(
    description = "Information about a call of a hand in a round of Doppelkopf."
)
class CallInfoDto(
    @field:Schema(
        description = "The UUID of this call."
    )
    val id: UUID,

    @field:Schema(
        description = "The UUID of the hand this call was made by."
    )
    val handId: UUID,

    @field:Schema(
        description = "The UUID of the round this call was made in."
    )
    val roundId: UUID,

    @field:Schema(
        description = "The type of this call."
    )
    val callType: CallType,

    @field:Schema(
        description = "The number of cards the hand had already played when this call was made."
    )
    val cardsPlayedBefore: Int
) {
    constructor(callEntity: CallEntity) : this(
        id = callEntity.id,
        handId = callEntity.hand.id,
        roundId = callEntity.hand.round.id,
        callType = callEntity.callType,
        cardsPlayedBefore = callEntity.cardsPlayedBefore,
    )
}