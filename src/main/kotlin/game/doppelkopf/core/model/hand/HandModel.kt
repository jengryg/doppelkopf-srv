package game.doppelkopf.core.model.hand

import game.doppelkopf.core.common.enums.Bidding
import game.doppelkopf.core.common.enums.BiddingOption
import game.doppelkopf.core.common.enums.Declaration
import game.doppelkopf.core.common.enums.DeclarationOption
import game.doppelkopf.core.common.errors.ofForbiddenAction
import game.doppelkopf.core.common.errors.ofInvalidAction
import game.doppelkopf.core.model.IModelFactory
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.persistence.model.hand.HandEntity
import org.springframework.lang.CheckReturnValue
import java.util.*

class HandModel private constructor(
    entity: HandEntity,
) : HandModelAbstract(entity) {
    fun declare(user: UserModel, declarationOption: DeclarationOption) {
        canDeclare(user, declarationOption).getOrThrow()

        declared = declarationOption.internal
    }

    @CheckReturnValue
    fun canDeclare(user: UserModel, declarationOption: DeclarationOption): Result<Unit> {
        return when {
            entity.player.user != user.entity -> Result.ofForbiddenAction(
                "Bid:Create",
                "You can only declare on your own hand."
            )

            declared != Declaration.NOTHING -> Result.ofInvalidAction(
                "Declaration:Create",
                "This hand has already made a declaration."
            )

            else -> when (declarationOption) {
                // always allow the RESERVATION type
                DeclarationOption.RESERVATION -> Result.success(Unit)

                DeclarationOption.HEALTHY -> when {
                    hasMarriage -> return Result.ofInvalidAction(
                        "Declaration:Create",
                        "You can not declare HEALTHY when you have a marriage on hand."
                    )

                    else -> return Result.success(Unit)
                }

                DeclarationOption.SILENT_MARRIAGE -> when {
                    hasMarriage -> return Result.success(Unit)
                    else -> Result.ofInvalidAction(
                        "Declaration:Create",
                        "You can not declare SILENT_MARRIAGE when you are not having a marriage on hand."
                    )
                }
            }
        }
    }

    /**
     * Make the [biddingOption] as [user] on this hand.
     */
    fun bid(user: UserModel, biddingOption: BiddingOption) {
        canBid(user, biddingOption).getOrThrow()

        bidding = biddingOption.internal
    }

    @CheckReturnValue
    fun canBid(user: UserModel, biddingOption: BiddingOption): Result<Unit> {
        return when {
            entity.player.user != user.entity -> Result.ofForbiddenAction(
                "Bid:Create",
                "You can only bid on your own hand."
            )

            bidding != Bidding.NOTHING -> Result.ofInvalidAction(
                "Bid:Create",
                "This hand has already made a bid."
            )

            declared != Declaration.RESERVATION -> Result.ofInvalidAction(
                "Bid:Create",
                "You did not declared a RESERVATION, thus you can not bid."
            )

            else -> when (biddingOption) {
                BiddingOption.MARRIAGE -> when {
                    hasMarriage -> return Result.success(Unit)
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

    companion object : IModelFactory<HandEntity, HandModel> {
        private val instances = mutableMapOf<UUID, HandModel>()

        override fun create(entity: HandEntity): HandModel {
            return instances.getOrPut(entity.id) { HandModel(entity) }
        }
    }
}