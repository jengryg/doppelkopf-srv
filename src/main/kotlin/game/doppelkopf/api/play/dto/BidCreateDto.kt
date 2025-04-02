package game.doppelkopf.api.play.dto

import game.doppelkopf.core.play.enums.BiddingOption
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Make a bid for the hand."
)
class BidCreateDto(
    @Schema(
        description = "The bid to make."
    )
    val bid: BiddingOption
)