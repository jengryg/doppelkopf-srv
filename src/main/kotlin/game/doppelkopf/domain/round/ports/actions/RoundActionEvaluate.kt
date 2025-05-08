package game.doppelkopf.domain.round.ports.actions

import game.doppelkopf.security.UserDetails
import java.util.*

/**
 * Evaluate the round results in the round given by [roundId].
 */
class RoundActionEvaluate(
    override val user: UserDetails,
    override val roundId: UUID
) : IRoundAction