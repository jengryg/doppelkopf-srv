package game.doppelkopf.domain.lobby.ports.commands

import game.doppelkopf.security.UserDetails

/**
 * Creates a new game that is owned by [user] and allows for maximum [playerLimit] players at the table.
 */
class LobbyCommandCreateNewGame(
    override val user: UserDetails,
    val playerLimit: Int
) : ILobbyCommand