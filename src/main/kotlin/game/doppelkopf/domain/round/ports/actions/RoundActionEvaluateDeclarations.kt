package game.doppelkopf.domain.round.ports.actions

import game.doppelkopf.security.UserDetails
import java.util.*

/**
 * Evaluate the declarations made by the hands in the round given by [roundId].
 */
class RoundActionEvaluateDeclarations(
    override val user: UserDetails,
    override val roundId: UUID
) : IRoundAction