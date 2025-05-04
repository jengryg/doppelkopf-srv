package game.doppelkopf.domain.game.model

import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.common.IBaseModel
import game.doppelkopf.domain.player.model.IPlayerModel
import game.doppelkopf.domain.round.model.IRoundModel
import game.doppelkopf.domain.user.IUserModel

interface IGameModel : IGameProperties, IBaseModel<GameEntity> {
    val creator: IUserModel
    val players: Map<IUserModel, IPlayerModel>
    val rounds: Map<Int, IRoundModel>
}