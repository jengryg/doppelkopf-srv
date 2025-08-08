package game.doppelkopf.adapter.api.core.trick.dto

import game.doppelkopf.adapter.api.core.hand.dto.HandPublicInfoResponse
import game.doppelkopf.adapter.persistence.model.trick.TrickEntity
import game.doppelkopf.domain.deck.enums.CardDemand
import game.doppelkopf.domain.trick.enums.TrickState
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

class TrickInfoResponse(
    @field:Schema(
        description = "The UUID of this trick."
    )
    val id: UUID,

    @field:Schema(
        description = "The UUID of the round this trick is played in."
    )
    val roundId: UUID,

    @field:Schema(
        description = "The (encoded) cards that this trick currently contains in order."
    )
    val cards: List<String>,

    @field:Schema(
        description = "The number of this trick with respect to the round."
    )
    val number: Int,

    @field:Schema(
        description = "The current state of this trick."
    )
    val state: TrickState,

    @field:Schema(
        description = "The trick was started at this moment. If this is null, the trick was not started yet."
    )
    val started: Instant?,

    @field:Schema(
        description = "The trick was ended at this moment. If this is null, the trick was not ended yet."
    )
    val ended: Instant?,

    @field:Schema(
        description = "The demand of this trick."
    )
    val demand: CardDemand,

    @field:Schema(
        description = "The index of the hand that played the first card of this hand."
    )
    val openIndex: Int,

    @field:Schema(
        description = "The index of the currently leading card of this trick with respect to the cards list."
    )
    val leadingCardIndex: Int,

    @field:Schema(
        description = "The winner of this trick if known, otherwise null."
    )
    val winner: HandPublicInfoResponse?,

    @field:Schema(
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
        winner = trickEntity.winner?.let { HandPublicInfoResponse(it) },
        score = trickEntity.score,
    )
}