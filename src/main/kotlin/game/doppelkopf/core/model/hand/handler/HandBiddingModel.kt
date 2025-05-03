package game.doppelkopf.core.model.hand.handler

import game.doppelkopf.core.common.enums.Bidding
import game.doppelkopf.core.common.enums.BiddingOption
import game.doppelkopf.core.common.enums.Declaration
import game.doppelkopf.core.errors.ofForbiddenAction
import game.doppelkopf.core.errors.ofInvalidAction
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.hand.HandModelAbstract
import game.doppelkopf.core.model.user.IUserModel
import game.doppelkopf.persistence.model.hand.HandEntity
import org.springframework.lang.CheckReturnValue

class HandBiddingModel(
    entity: HandEntity,
    factoryProvider: ModelFactoryProvider
) : HandModelAbstract(entity, factoryProvider) {
    fun bid(user: IUserModel, biddingOption: BiddingOption) {
        canBid(user, biddingOption).getOrThrow()

        bidding = biddingOption.internal
    }

    @CheckReturnValue
    fun canBid(user: IUserModel, biddingOption: BiddingOption): Result<Unit> {
        return when {
            entity.player.user != user.entity -> forbidden("You can only bid on your own hand.")
            bidding != Bidding.NOTHING -> invalid("This hand has already made a bid.")
            declared != Declaration.RESERVATION -> invalid("You did not declared a RESERVATION, thus you can not bid.")
            else -> when (biddingOption) {
                BiddingOption.MARRIAGE -> when {
                    hasMarriage -> return Result.success(Unit)
                    else -> invalid("You can only bid WEDDING when you have a marriage on hand.")
                }

                // TODO: SOLO SYSTEM IMPLEMENTATION
                // all other options are solos, and those are always allowed to bid
                // else -> Result.success(Unit)
            }
        }
    }

    companion object {
        const val ACTION = "Bidding:Create"

        fun forbidden(reason: String): Result<Unit> {
            return Result.ofForbiddenAction(ACTION, reason)
        }

        fun invalid(reason: String): Result<Unit> {
            return Result.ofInvalidAction(ACTION, reason)
        }
    }
}