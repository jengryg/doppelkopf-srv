package game.doppelkopf.core.model.player

import game.doppelkopf.core.common.IBaseModel
import game.doppelkopf.core.model.game.IGameModel
import game.doppelkopf.core.model.round.IRoundModel
import game.doppelkopf.core.model.user.IUserModel
import game.doppelkopf.persistence.model.player.PlayerEntity

interface IPlayerModel : IPlayerProperties, IBaseModel<PlayerEntity> {
    val game: IGameModel
    val user: IUserModel
    val dealtRounds: List<IRoundModel>
}