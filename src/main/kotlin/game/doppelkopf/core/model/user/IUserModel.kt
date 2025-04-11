package game.doppelkopf.core.model.user

import game.doppelkopf.core.common.IBaseModel
import game.doppelkopf.core.model.game.IGameModel
import game.doppelkopf.core.model.player.IPlayerModel
import game.doppelkopf.persistence.model.user.UserEntity

interface IUserModel : IUserProperties, IBaseModel<UserEntity> {
    val createdGames: List<IGameModel>
    val players: List<IPlayerModel>
}