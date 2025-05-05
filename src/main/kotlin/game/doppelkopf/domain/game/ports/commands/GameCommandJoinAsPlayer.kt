package game.doppelkopf.domain.game.ports.commands

import game.doppelkopf.security.UserDetails
import java.util.*

/**
 * Join the game given by [gameId] where [user] is added as player to the game.
 * The position at the table is indicated by the [seat] number.
 */
class GameCommandJoinAsPlayer(
    override val user: UserDetails,
    override val gameId: UUID,
    val seat: Int
) : IGameCommand {
    override fun getSlug() = "Game:Join"
}