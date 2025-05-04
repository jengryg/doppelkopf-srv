package game.doppelkopf.domain.player.model

import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.common.model.IBaseModel
import game.doppelkopf.domain.game.model.IGameModel
import game.doppelkopf.domain.user.model.IUserModel

interface IPlayerModel : IPlayerProperties, IBaseModel<PlayerEntity> {
    val user: IUserModel
    val game: IGameModel
}