package game.doppelkopf.adapter.graphql.core.trick.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.adapter.graphql.common.StartedEnded
import game.doppelkopf.adapter.graphql.core.hand.dto.PublicHand
import game.doppelkopf.adapter.graphql.core.round.dto.Round
import game.doppelkopf.adapter.persistence.model.trick.TrickEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.domain.deck.enums.CardDemand
import java.util.*

data class Trick(
    val id: UUID,
    val cards: List<String>,
    val number: Int,
    val demand: CardDemand,
    val openIndex: Int,
    val leadingCardIndex: Int,
    private val _cu: Lazy<CreatedUpdated>,
    private val _se: Lazy<StartedEnded>,
    private val _round: Lazy<Round>,
    private val _winner: Lazy<PublicHand?>,
) {
    val cu: CreatedUpdated by _cu
    val se: StartedEnded by _se
    val round: Round by _round
    val winner: PublicHand? by _winner

    constructor(entity: TrickEntity, currentUser: UserEntity) : this(
        id = entity.id,
        cards = entity.cards,
        number = entity.number,
        demand = entity.demand,
        openIndex = entity.openIndex,
        leadingCardIndex = entity.leadingCardIndex,
        _cu = lazy { CreatedUpdated(entity) },
        _se = lazy { StartedEnded(entity) },
        _round = lazy { Round(entity.round, currentUser) },
        _winner = lazy { entity.winner?.let { PublicHand(it, currentUser) } },
    )
}
