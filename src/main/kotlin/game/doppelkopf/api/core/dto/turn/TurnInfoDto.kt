package game.doppelkopf.api.core.dto.turn

import game.doppelkopf.persistence.model.turn.TurnEntity
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(
    description = "Information about a turn in a round of Doppelkopf."
)
class TurnInfoDto(
    @Schema(
        description = "The UUID of this turn."
    )
    val id: UUID,

    @Schema(
        description = "The UUID of the round this turn is made in."
    )
    val roundId: UUID,

    @Schema(
        description = "The UUID of the hand this turn is made by."
    )
    val handId: UUID,

    @Schema(
        description = "The UUID of the trick this turn is made in."
    )
    val trickId: UUID,

    @Schema(
        description = "The number of this turn, incrementally starting at 1."
    )
    val number: Int,

    @Schema(
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