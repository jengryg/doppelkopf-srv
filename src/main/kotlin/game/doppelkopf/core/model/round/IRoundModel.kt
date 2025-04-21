package game.doppelkopf.core.model.round

import game.doppelkopf.core.model.IBaseModel
import game.doppelkopf.core.model.game.IGameModel
import game.doppelkopf.core.model.hand.IHandModel
import game.doppelkopf.core.model.player.IPlayerModel
import game.doppelkopf.core.model.user.IUserModel
import game.doppelkopf.persistence.model.round.RoundEntity

interface IRoundModel : IRoundProperties, IBaseModel<RoundEntity> {
    val game: IGameModel
    val dealer: IPlayerModel
    val hands: Map<IUserModel, IHandModel>
}