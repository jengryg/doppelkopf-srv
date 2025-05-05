package game.doppelkopf.domain.round.ports.actions

import game.doppelkopf.security.UserDetails
import java.util.*

/**
 * Evaluate the bids made by the hands in the round given by [roundId].
 */
class RoundActionEvaluateBids(
    override val user: UserDetails,
    override val roundId: UUID
) : IRoundAction