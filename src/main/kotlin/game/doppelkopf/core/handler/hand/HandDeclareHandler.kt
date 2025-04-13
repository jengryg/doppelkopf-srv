package game.doppelkopf.core.handler.hand

import game.doppelkopf.core.common.enums.Declaration
import game.doppelkopf.core.common.enums.DeclarationOption
import game.doppelkopf.core.common.errors.ofInvalidAction
import game.doppelkopf.core.model.hand.HandModel
import game.doppelkopf.persistence.model.hand.HandEntity
import org.springframework.lang.CheckReturnValue

class HandDeclareHandler(
    val hand: HandModel
) {
    fun doHandle(declaration: DeclarationOption): HandEntity {
        canHandle(declaration).getOrThrow()

        hand.declared = declaration.internal

        return hand.entity
    }

    @CheckReturnValue
    fun canHandle(declaration: DeclarationOption): Result<Unit> {
        return when {
            hand.declared != Declaration.NOTHING -> Result.ofInvalidAction(
                "Declaration:Create",
                "This hand has already made a declaration."
            )

            else -> when (declaration) {
                // always allow the RESERVATION type
                DeclarationOption.RESERVATION -> Result.success(Unit)

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
    }
}