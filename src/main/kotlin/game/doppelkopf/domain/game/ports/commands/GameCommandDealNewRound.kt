package game.doppelkopf.domain.game.ports.commands

import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity

class GameCommandDealNewRound(
    val user: UserEntity,
    val game: GameEntity
) : IGameCommand