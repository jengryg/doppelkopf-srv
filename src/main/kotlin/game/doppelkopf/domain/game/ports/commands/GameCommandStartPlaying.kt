package game.doppelkopf.domain.game.ports.commands

import game.doppelkopf.security.UserDetails
import java.util.*

/**
 * Start the game given by [gameId] where [user] should be the game manager.
 */
class GameCommandStartPlaying(
    override val user: UserDetails,
    override val gameId: UUID,
) : IGameCommand {
    override fun getSlug() = "Game:Start"
}