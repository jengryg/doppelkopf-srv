package game.doppelkopf.core.model.game

import game.doppelkopf.core.common.IBaseModel
import game.doppelkopf.core.model.player.IPlayerModel
import game.doppelkopf.core.model.round.IRoundModel
import game.doppelkopf.core.model.user.IUserModel
import game.doppelkopf.persistence.model.game.GameEntity

/**
 * [IGameModel] defines the final contract for the [GameModel] to enable automatic delegation of [IGameProperties]
 * and demand manual delegation of [IGameModel] properties not inherited from [IGameProperties] and [IBaseModel].
 */
interface IGameModel : IGameProperties, IBaseModel<GameEntity> {
    val creator: IUserModel

    val players: List<IPlayerModel>
    fun addPlayer(player: IPlayerModel)

    val rounds: List<IRoundModel>
    fun addRound(round: IRoundModel)
}