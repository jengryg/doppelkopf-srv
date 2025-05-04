package game.doppelkopf.core.model.turn

import game.doppelkopf.adapter.persistence.model.turn.TurnEntity
import game.doppelkopf.common.IBaseModel
import game.doppelkopf.core.cards.Card
import game.doppelkopf.core.model.hand.IHandModel
import game.doppelkopf.core.model.round.IRoundModel
import game.doppelkopf.core.model.trick.ITrickModel

interface ITurnModel : ITurnProperties, IBaseModel<TurnEntity> {
    val round: IRoundModel
    val hand: IHandModel
    val trick: ITrickModel
    val card: Card
}