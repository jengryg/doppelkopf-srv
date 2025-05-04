package game.doppelkopf.domain.turn.model

import game.doppelkopf.adapter.persistence.model.turn.TurnEntity
import game.doppelkopf.common.model.IBaseModel
import game.doppelkopf.domain.deck.model.Card
import game.doppelkopf.domain.hand.model.IHandModel
import game.doppelkopf.domain.round.model.IRoundModel
import game.doppelkopf.domain.trick.model.ITrickModel

interface ITurnModel : ITurnProperties, IBaseModel<TurnEntity> {
    val round: IRoundModel
    val hand: IHandModel
    val trick: ITrickModel
    val card: Card
}