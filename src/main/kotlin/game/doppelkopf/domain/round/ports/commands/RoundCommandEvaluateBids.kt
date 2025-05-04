package game.doppelkopf.domain.round.ports.commands

import game.doppelkopf.security.UserDetails
import java.util.*

/**
 * Evaluate the bids made by the hands in the round given by [roundId].
 */
class RoundCommandEvaluateBids(
    override val user: UserDetails,
    override val roundId: UUID
) : IRoundCommand {
    override fun getSlug() = "Round:EvaluateBids"
}