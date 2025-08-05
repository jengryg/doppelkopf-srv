package game.doppelkopf.adapter.api.core.turn.dto

import game.doppelkopf.adapter.persistence.model.turn.TurnEntity
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(
    description = "Information about a turn in a round of Doppelkopf."
)
class TurnInfoDto(
    @field:Schema(
        description = "The UUID of this turn."
    )
    val id: UUID,

    @field:Schema(
        description = "The UUID of the round this turn is made in."
    )
    val roundId: UUID,

    @field:Schema(
        description = "The UUID of the hand this turn is made by."
    )
    val handId: UUID,

    @field:Schema(
        description = "The UUID of the trick this turn is made in."
    )
    val trickId: UUID,

    @field:Schema(
        description = "The number of this turn, incrementally starting at 1."
    )
    val number: Int,

    @field:Schema(
        description = "The card played in this turn."
    )
    val card: String
) {
    constructor(turnEntity: TurnEntity) : this(
        id = turnEntity.id,
        roundId = turnEntity.round.id,
        handId = turnEntity.hand.id,
        trickId = turnEntity.trick.id,
        number = turnEntity.number,
        card = turnEntity.card,
    )
}