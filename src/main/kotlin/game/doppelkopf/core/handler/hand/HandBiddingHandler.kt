package game.doppelkopf.core.handler.hand

import game.doppelkopf.core.common.enums.Bidding
import game.doppelkopf.core.common.enums.BiddingOption
import game.doppelkopf.core.common.enums.Declaration
import game.doppelkopf.core.common.errors.ofInvalidAction
import game.doppelkopf.core.model.hand.HandModel
import game.doppelkopf.persistence.model.hand.HandEntity
import org.springframework.lang.CheckReturnValue

class HandBiddingHandler(
    val hand: HandModel
) {
    fun doHandle(bidding: BiddingOption): HandEntity {
        canHandle(bidding).getOrThrow()

        hand.bidding = bidding.internal

        return hand.entity
    }

    @CheckReturnValue
    fun canHandle(bidding: BiddingOption): Result<Unit> {
        return when {
            hand.bidding != Bidding.NOTHING -> Result.ofInvalidAction(
                "Bid:Create",
                "This hand has already made a bid."
            )

            hand.declared != Declaration.RESERVATION -> Result.ofInvalidAction(
                "Bid:Create",
                "You did not declared a RESERVATION, thus you can not bid."
            )

            else -> when (bidding) {
                BiddingOption.WEDDING -> when {
                    hand.hasMarriage -> return Result.success(Unit)
                    else -> Result.ofInvalidAction(
                        "Bid:Create",
                        "You can only bid WEDDING when you have a marriage on hand."
                    )
                }

                // TODO: SOLO SYSTEM IMPLEMENTATION
                // all other options are solos, and those are always allowed to bid
                // else -> Result.success(Unit)
            }
        }
    }
}