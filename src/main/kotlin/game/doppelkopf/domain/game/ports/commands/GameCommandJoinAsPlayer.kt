package game.doppelkopf.domain.game.ports.commands

import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity

class GameCommandJoinAsPlayer(
    val user: UserEntity,
    val game: GameEntity,
    val seat: Int
) : IGameCommand