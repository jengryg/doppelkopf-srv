package game.doppelkopf.domain.lobby.ports.actions

import game.doppelkopf.security.UserDetails

/**
 * Creates a new game that is owned by [user] and allows for maximum [playerLimit] players at the table.
 */
class LobbyActionCreateNewGame(
    override val user: UserDetails,
    val playerLimit: Int,
    val seed: ByteArray?,
) : ILobbyAction