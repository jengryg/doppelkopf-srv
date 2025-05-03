package game.doppelkopf.core.model.trick

import game.doppelkopf.core.cards.Card
import game.doppelkopf.core.common.enums.TrickState
import game.doppelkopf.common.IBaseModel
import game.doppelkopf.core.model.hand.IHandModel
import game.doppelkopf.core.model.round.IRoundModel
import game.doppelkopf.persistence.model.trick.TrickEntity

interface ITrickModel : ITrickProperties, IBaseModel<TrickEntity> {
    val round: IRoundModel
    val winner: IHandModel?
    fun setWinner(winner: IHandModel)
    val size: Int
    val cards: List<Card>

    fun getExpectedHandIndex(): Int
    fun determineLeadingCardIndex(): Int
    fun determineScoreFromCards(): Int
    fun determineStateFromCards(): TrickState
    fun updateCachedValues()
}