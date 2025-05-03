package game.doppelkopf.core.model.round

import game.doppelkopf.core.cards.Deck
import game.doppelkopf.common.IBaseModel
import game.doppelkopf.core.model.game.IGameModel
import game.doppelkopf.core.model.hand.IHandModel
import game.doppelkopf.core.model.player.IPlayerModel
import game.doppelkopf.core.model.trick.ITrickModel
import game.doppelkopf.core.model.user.IUserModel
import game.doppelkopf.adapter.persistence.model.round.RoundEntity

interface IRoundModel : IRoundProperties, IBaseModel<RoundEntity> {
    val game: IGameModel
    val dealer: IPlayerModel
    val hands: Map<IUserModel, IHandModel>
    val deck: Deck





    val tricks: Map<Int, ITrickModel>
}