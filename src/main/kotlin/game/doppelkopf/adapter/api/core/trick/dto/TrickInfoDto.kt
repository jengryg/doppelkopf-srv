package game.doppelkopf.adapter.api.core.trick.dto

import game.doppelkopf.adapter.api.core.hand.dto.HandPublicInfoDto
import game.doppelkopf.adapter.persistence.model.trick.TrickEntity
import game.doppelkopf.domain.deck.enums.CardDemand
import game.doppelkopf.domain.trick.enums.TrickState
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

class TrickInfoDto(
    @Schema(
        description = "The UUID of this trick."
    )
    val id: UUID,

    @Schema(
        description = "The UUID of the round this trick is played in."
    )
    val roundId: UUID,

    @Schema(
        description = "The (encoded) cards that this trick currently contains in order."
    )
    val cards: List<String>,

    @Schema(
        description = "The number of this trick with respect to the round."
    )
    val number: Int,

    @Schema(
        description = "The current state of this trick."
    )
    val state: TrickState,

    @Schema(
        description = "The trick was started at this moment. If this is null, the trick was not started yet."
    )
    val started: Instant?,

    @Schema(
        description = "The trick was ended at this moment. If this is null, the trick was not ended yet."
    )
    val ended: Instant?,

    @Schema(
        description = "The demand of this trick."
    )
    val demand: CardDemand,

    @Schema(
        description = "The index of the hand that played the first card of this hand."
    )
    val openIndex: Int,

    @Schema(
        description = "The index of the currently leading card of this trick with respect to the cards list."
    )
    val leadingCardIndex: Int,

    @Schema(
        description = "The winner of this trick if known, otherwise null."
    )
    val winner: HandPublicInfoDto?,

    @Schema(
        description = "The current score of this trick."
    )
    val score: Int,
) {
    constructor(
        trickEntity: TrickEntity
    ) : this(
        id = trickEntity.id,
        roundId = trickEntity.round.id,
        cards = trickEntity.cards.toList(),
        number = trickEntity.number,
        state = trickEntity.state,
        started = trickEntity.started,
        ended = trickEntity.ended,
        demand = trickEntity.demand,
        openIndex = trickEntity.openIndex,
        leadingCardIndex = trickEntity.leadingCardIndex,
        winner = trickEntity.winner?.let { HandPublicInfoDto(it) },
        score = trickEntity.score,
    )
}