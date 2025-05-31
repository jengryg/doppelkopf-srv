package game.doppelkopf.adapter.graphql.core.trick.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.adapter.graphql.common.StartedEnded
import game.doppelkopf.domain.deck.enums.CardDemand
import java.util.*

data class TrickResponse(
    val id: UUID,
    val cu: CreatedUpdated,
    val se: StartedEnded,
    val cards: List<String>,
    val number: Int,
    val demand: CardDemand,
    val openIndex: Int,
    val leadingCardIndex: Int,
)