package game.doppelkopf.domain.round.ports.actions

import game.doppelkopf.security.UserDetails
import java.util.*

/**
 * Resolve the marriage in the round given by [roundId].
 */
class RoundActionResolveMarriage(
    override val user: UserDetails,
    override val roundId: UUID
) : IRoundAction