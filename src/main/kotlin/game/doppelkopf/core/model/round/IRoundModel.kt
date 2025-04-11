package game.doppelkopf.core.model.round

import game.doppelkopf.core.cards.Deck
import game.doppelkopf.core.common.IBaseModel
import game.doppelkopf.core.model.game.IGameModel
import game.doppelkopf.core.model.hand.IHandModel
import game.doppelkopf.core.model.player.IPlayerModel
import game.doppelkopf.persistence.model.round.RoundEntity

interface IRoundModel : IRoundProperties, IBaseModel<RoundEntity> {
    val game: IGameModel
    val dealer: IPlayerModel
    val deck: Deck
    val hands: List<IHandModel>
}