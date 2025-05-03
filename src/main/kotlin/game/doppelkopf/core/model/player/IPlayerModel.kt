package game.doppelkopf.core.model.player

import game.doppelkopf.common.IBaseModel
import game.doppelkopf.core.model.game.IGameModel
import game.doppelkopf.core.model.user.IUserModel
import game.doppelkopf.persistence.model.player.PlayerEntity

interface IPlayerModel : IPlayerProperties, IBaseModel<PlayerEntity> {
    val user: IUserModel
    val game: IGameModel
}