package game.doppelkopf.domain.hand.ports.commands

import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.domain.hand.enums.DeclarationOption

class HandCommandDeclare(
    val user: UserEntity,
    val hand: HandEntity,
    val declarationOption: DeclarationOption
) : IHandCommand