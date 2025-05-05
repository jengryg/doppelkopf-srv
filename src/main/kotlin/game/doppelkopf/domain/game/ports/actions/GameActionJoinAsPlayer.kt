package game.doppelkopf.domain.game.ports.actions

import game.doppelkopf.security.UserDetails
import java.util.*

/**
 * Join the game given by [gameId] where [user] is added as player to the game.
 * The position at the table is indicated by the [seat] number.
 */
class GameActionJoinAsPlayer(
    override val user: UserDetails,
    override val gameId: UUID,
    val seat: Int
) : IGameAction