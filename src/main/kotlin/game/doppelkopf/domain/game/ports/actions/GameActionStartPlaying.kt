package game.doppelkopf.domain.game.ports.actions

import game.doppelkopf.security.UserDetails
import java.util.*

/**
 * Start the game given by [gameId] where [user] should be the game manager.
 */
class GameActionStartPlaying(
    override val user: UserDetails,
    override val gameId: UUID,
) : IGameAction