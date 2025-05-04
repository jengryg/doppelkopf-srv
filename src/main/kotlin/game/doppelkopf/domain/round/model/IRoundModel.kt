package game.doppelkopf.domain.round.model

import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.common.IBaseModel
import game.doppelkopf.core.cards.Deck
import game.doppelkopf.domain.game.model.IGameModel
import game.doppelkopf.domain.hand.model.IHandModel
import game.doppelkopf.domain.player.model.IPlayerModel
import game.doppelkopf.domain.trick.model.ITrickModel
import game.doppelkopf.domain.user.IUserModel

interface IRoundModel : IRoundProperties, IBaseModel<RoundEntity> {
    val game: IGameModel
    val dealer: IPlayerModel
    val hands: Map<IUserModel, IHandModel>
    val deck: Deck
    val tricks: Map<Int, ITrickModel>
}