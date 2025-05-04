package game.doppelkopf.domain.round.ports.commands

import game.doppelkopf.security.UserDetails
import java.util.*

/**
 * Play the [encodedCard] in the round given by [roundId] with [user] being the player that plays the card.
 */
class RoundCommandPlayCard(
    override val user: UserDetails,
    override val roundId: UUID,
    val encodedCard: String,
) : IRoundCommand {
    override fun getSlug() = "Round:PlayCard"
}