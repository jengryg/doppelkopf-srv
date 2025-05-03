package game.doppelkopf.core.model.game

import game.doppelkopf.common.IBaseModel
import game.doppelkopf.core.model.player.IPlayerModel
import game.doppelkopf.core.model.round.IRoundModel
import game.doppelkopf.core.model.user.IUserModel
import game.doppelkopf.adapter.persistence.model.game.GameEntity

interface IGameModel : IGameProperties, IBaseModel<GameEntity> {
    val creator: IUserModel
    val players: Map<IUserModel, IPlayerModel>
    val rounds: Map<Int, IRoundModel>
}