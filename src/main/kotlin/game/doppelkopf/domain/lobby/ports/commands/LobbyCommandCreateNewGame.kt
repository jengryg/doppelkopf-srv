package game.doppelkopf.domain.lobby.ports.commands

import game.doppelkopf.adapter.persistence.model.user.UserEntity

class LobbyCommandCreateNewGame(
    val user: UserEntity,
    val playerLimit: Int
) : ILobbyCommand