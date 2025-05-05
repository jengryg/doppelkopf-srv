package game.doppelkopf.domain.round.ports.actions

import game.doppelkopf.security.UserDetails
import java.util.*

/**
 * Play the [encodedCard] in the round given by [roundId] with [user] being the player that plays the card.
 */
class RoundActionPlayCard(
    override val user: UserDetails,
    override val roundId: UUID,
    val encodedCard: String,
) : IRoundAction