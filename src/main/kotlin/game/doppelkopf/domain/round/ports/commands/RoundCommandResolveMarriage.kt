package game.doppelkopf.domain.round.ports.commands

import game.doppelkopf.security.UserDetails
import java.util.UUID

/**
 * Resolve the marriage in the round given by [roundId].
 */
class RoundCommandResolveMarriage(
    override val user: UserDetails,
    override val roundId: UUID
) : IRoundCommand