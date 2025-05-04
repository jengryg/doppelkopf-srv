package game.doppelkopf.domain.hand.ports.commands

import game.doppelkopf.domain.hand.enums.BiddingOption
import game.doppelkopf.security.UserDetails
import java.util.*

/**
 * Bid the [bid] on the hand given by [handId] where [user] must be the player of the hand.
 */
class HandCommandBid(
    override val user: UserDetails,
    override val handId: UUID,
    val bid: BiddingOption
) : IHandCommand {
    override fun getSlug() = "Hand:Bid"
}