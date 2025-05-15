package game.doppelkopf.domain.hand.ports.actions

import game.doppelkopf.domain.call.enums.CallType
import game.doppelkopf.security.UserDetails
import java.util.*

/**
 * Create a call of [callType] on the hand given by [handId] where [user] must be the player of the hand.
 */
class HandActionCall(
    override val user: UserDetails,
    override val handId: UUID,
    val callType: CallType
) : IHandAction