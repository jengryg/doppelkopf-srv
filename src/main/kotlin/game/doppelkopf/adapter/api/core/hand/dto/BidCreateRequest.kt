package game.doppelkopf.adapter.api.core.hand.dto

import game.doppelkopf.domain.hand.enums.BiddingOption
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Make a bid for the hand."
)
class BidCreateRequest(
    @field:Schema(
        description = "The bid to make."
    )
    val bid: BiddingOption
)