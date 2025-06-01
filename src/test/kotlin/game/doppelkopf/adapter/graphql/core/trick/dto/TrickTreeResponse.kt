package game.doppelkopf.adapter.graphql.core.trick.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.adapter.graphql.common.StartedEnded
import game.doppelkopf.adapter.graphql.core.hand.dto.PublicHandTreeResponse
import game.doppelkopf.domain.deck.enums.CardDemand
import java.util.*

data class TrickTreeResponse(
    val id: UUID,
    val cu: CreatedUpdated,
    val se: StartedEnded,
    val cards: List<String>,
    val number: Int,
    val demand: CardDemand,
    val openIndex: Int,
    val leadingCardIndex: Int,
    val winner: PublicHandTreeResponse?,
)
