package game.doppelkopf.core.model.user

import game.doppelkopf.core.model.game.GameModel
import game.doppelkopf.core.model.game.IGameModel
import game.doppelkopf.core.model.player.IPlayerModel
import game.doppelkopf.core.model.player.PlayerModel
import game.doppelkopf.persistence.model.user.UserEntity

class UserModel(
    override val entity: UserEntity
) : IUserProperties by entity, IUserModel {
    override val createdGames: List<IGameModel>
        get() = entity.createdGames.map { GameModel(it) }

    override val players: List<IPlayerModel>
        get() = entity.players.map { PlayerModel(it) }

}