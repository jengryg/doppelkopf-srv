package game.doppelkopf.core.model.hand.handler

import game.doppelkopf.core.common.enums.Declaration
import game.doppelkopf.core.common.enums.DeclarationOption
import game.doppelkopf.core.common.errors.ofForbiddenAction
import game.doppelkopf.core.common.errors.ofInvalidAction
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.hand.HandModelAbstract
import game.doppelkopf.core.model.user.IUserModel
import game.doppelkopf.persistence.model.hand.HandEntity
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