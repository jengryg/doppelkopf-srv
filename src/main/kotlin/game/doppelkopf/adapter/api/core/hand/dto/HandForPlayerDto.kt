package game.doppelkopf.adapter.api.core.hand.dto

import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.domain.hand.enums.Team
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(
    description = "Information about a hand in a game of Doppelkopf that should only be available to the player of the hand."
)
class HandForPlayerDto(
    @field:Schema(
        description = "The UUID of this hand."
    )
    val id: UUID,

    @field:Schema(
        description = "The UUID of the round this hand is played in."
    )
    val roundId: UUID,

    @field:Schema(
        description = "The UUID of the player this hand is played by."
    )
    val playerId: UUID,

    @field:Schema(
        description = "The cards that this hand has still to play."
    )
    val cardsRemaining: List<String>,

    @field:Schema(
        description = "The cards that were already played by this hand."
    )
    val cardsPlayed: List<String>,

    @field:Schema(
        description = "The team this hands plays on, from the viewpoint of the player of this hand."
    )
    val playerTeam: Team,
) {
    constructor(handEntity: HandEntity) : this(
        id = handEntity.id,
        roundId = handEntity.round.id,
        playerId = handEntity.player.id,
        cardsRemaining = handEntity.cardsRemaining,
        cardsPlayed = handEntity.cardsPlayed,
        playerTeam = handEntity.playerTeam
    )
}