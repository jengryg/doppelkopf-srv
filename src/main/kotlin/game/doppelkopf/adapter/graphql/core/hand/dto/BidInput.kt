package game.doppelkopf.adapter.graphql.core.hand.dto

import game.doppelkopf.domain.hand.enums.BiddingOption
import java.util.*

data class BidInput(
    val handId: UUID,
    val bid: BiddingOption,
)
