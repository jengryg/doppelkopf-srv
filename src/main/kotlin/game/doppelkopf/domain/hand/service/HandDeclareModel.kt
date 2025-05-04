package game.doppelkopf.domain.hand.service

import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.domain.hand.enums.Declaration
import game.doppelkopf.domain.hand.enums.DeclarationOption
import game.doppelkopf.common.errors.ofForbiddenAction
import game.doppelkopf.common.errors.ofInvalidAction
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.hand.model.HandModelAbstract
import game.doppelkopf.domain.user.model.IUserModel
import org.springframework.lang.CheckReturnValue

class HandDeclareModel(
    entity: HandEntity,
    factoryProvider: ModelFactoryProvider
) : HandModelAbstract(entity, factoryProvider) {
    /**
     * Declare [declarationOption] on this hand as [user].
     */
    fun declare(user: IUserModel, declarationOption: DeclarationOption) {
        canDeclare(user, declarationOption).getOrThrow()

        declared = declarationOption.internal
    }

    /**
     * Check if all conditions for [user] to declare [declarationOption] on this hand are satisfied.
     */
    @CheckReturnValue
    fun canDeclare(user: IUserModel, declarationOption: DeclarationOption): Result<Unit> {
        return when {
            entity.player.user != user.entity -> forbidden("You can only declare on your own hand.")
            declared != Declaration.NOTHING -> invalid("This hand has already made a declaration.")
            else -> when (declarationOption) {
                // always allow the RESERVATION type
                DeclarationOption.RESERVATION -> Result.success(Unit)

                DeclarationOption.HEALTHY -> when {
                    hasMarriage -> invalid("You can not declare HEALTHY when you have a marriage on hand.")
                    else -> return Result.success(Unit)
                }

                DeclarationOption.SILENT_MARRIAGE -> when {
                    hasMarriage -> return Result.success(Unit)
                    else -> invalid("You can not declare SILENT_MARRIAGE when you are not having a marriage on hand.")
                }
            }
        }
    }

    companion object {
        const val ACTION = "Declaration:Create"

        fun forbidden(reason: String): Result<Unit> {
            return Result.ofForbiddenAction(ACTION, reason)
        }

        fun invalid(reason: String): Result<Unit> {
            return Result.ofInvalidAction(ACTION, reason)
        }
    }
}