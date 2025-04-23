package game.doppelkopf.core.model.trick

import game.doppelkopf.core.cards.Card
import game.doppelkopf.core.model.IBaseModel
import game.doppelkopf.core.model.hand.IHandModel
import game.doppelkopf.core.model.round.IRoundModel
import game.doppelkopf.persistence.model.trick.TrickEntity

interface ITrickModel : ITrickProperties, IBaseModel<TrickEntity> {
    val round: IRoundModel
    val winner: IHandModel?
    val size: Int
    val cards: List<Card>
}