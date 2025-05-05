package game.doppelkopf.domain.game.ports.commands

import game.doppelkopf.security.UserDetails
import java.util.*

/**
 * Deal a new round in the game given by [gameId] where [user] should be the dealer.
 */
class GameCommandDealNewRound(
    override val user: UserDetails,
    override val gameId: UUID,
) : IGameCommand {
    override fun getSlug() = "Game:Deal"
}