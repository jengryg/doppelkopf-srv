package game.doppelkopf.core.model.turn

import game.doppelkopf.core.cards.Card
import game.doppelkopf.common.IBaseModel
import game.doppelkopf.core.model.hand.IHandModel
import game.doppelkopf.core.model.round.IRoundModel
import game.doppelkopf.core.model.trick.ITrickModel
import game.doppelkopf.persistence.model.turn.TurnEntity

interface ITurnModel : ITurnProperties, IBaseModel<TurnEntity> {
    val round: IRoundModel
    val hand: IHandModel
    val trick: ITrickModel
    val card: Card
}