package game.doppelkopf.core.play.model

import game.doppelkopf.core.common.errors.ofInvalidAction
import game.doppelkopf.core.common.enums.Bidding
import game.doppelkopf.core.common.enums.BiddingOption
import game.doppelkopf.core.common.enums.Declaration
import game.doppelkopf.core.common.enums.DeclarationOption
import game.doppelkopf.persistence.play.HandEntity
import org.springframework.lang.CheckReturnValue

class HandModel(
    val hand: HandEntity
) {
    fun declare(declaration: DeclarationOption)  {
        canDeclare(declaration).getOrThrow()

        hand.declared = declaration.internal
    }

    @CheckReturnValue
    fun canDeclare(declaration: DeclarationOption): Result<Unit> {
        if (hasDeclared()) {
            return Result.ofInvalidAction(
                "Declaration:Create",
                "This hand has already made a declaration."
            )
        }

        return when (declaration) {
            DeclarationOption.RESERVATION -> return Result.success(Unit)

            DeclarationOption.HEALTHY -> when {
                hand.hasMarriage -> return Result.ofInvalidAction(
                    "Declaration:Create",
                    "You can not declare HEALTHY when you have a marriage on hand."
                )

                else -> return Result.success(Unit)
            }

            DeclarationOption.SILENT_MARRIAGE -> when {
                hand.hasMarriage -> return Result.success(Unit)
                else -> Result.ofInvalidAction(
                    "Declaration:Create",
                    "You can not declare SILENT_MARRIAGE when you are not having a marriage on hand."
                )
            }

        }
    }

    fun bid(bid: BiddingOption) {
        canBid(bid).getOrThrow()

        hand.bidding = bid.internal
    }

    @CheckReturnValue
    fun canBid(bid: BiddingOption): Result<Unit> {
        if (hasBid()) {
            return Result.ofInvalidAction(
                "Bid:Create",
                "This hand has already made a bid."
            )
        }

        if (!hasReservation()) {
            return Result.ofInvalidAction(
                "Bid:Create",
                "You did not declared a RESERVATION, thus you can not bid."
            )
        }

        return when (bid) {
            BiddingOption.WEDDING -> when {
                hand.hasMarriage -> return Result.success(Unit)
                else -> Result.ofInvalidAction(
                    "Bid:Create",
                    "You can only bid WEDDING when you have a marriage on hand."
                )
            }

            // all other options are solos, and those are always allowed to bid
            else -> Result.success(Unit)
        }
    }

    private fun hasDeclared(): Boolean {
        return hand.declared != Declaration.NOTHING
    }

    private fun hasBid(): Boolean {
        return hand.bidding != Bidding.NOTHING
    }

    private fun hasReservation(): Boolean {
        return hand.declared == Declaration.RESERVATION
    }
}