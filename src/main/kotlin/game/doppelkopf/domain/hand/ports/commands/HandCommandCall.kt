package game.doppelkopf.domain.hand.ports.commands

import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.domain.call.enums.CallType

class HandCommandCall(
    val user: UserEntity,
    val hand: HandEntity,
    val callType: CallType
) : IHandCommand