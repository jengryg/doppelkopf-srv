package game.doppelkopf.core.model.game

import game.doppelkopf.core.model.IBaseModel
import game.doppelkopf.core.model.player.IPlayerModel
import game.doppelkopf.core.model.round.IRoundModel
import game.doppelkopf.core.model.user.IUserModel
import game.doppelkopf.persistence.model.game.GameEntity

interface IGameModel : IGameProperties, IBaseModel<GameEntity> {
    val creator: IUserModel
    val players: Map<IUserModel, IPlayerModel>
    val rounds: Map<Int, IRoundModel>
}