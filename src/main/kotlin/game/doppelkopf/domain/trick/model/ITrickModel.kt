package game.doppelkopf.domain.trick.model

import game.doppelkopf.adapter.persistence.model.trick.TrickEntity
import game.doppelkopf.common.model.IBaseModel
import game.doppelkopf.domain.deck.model.Card
import game.doppelkopf.domain.trick.enums.TrickState
import game.doppelkopf.domain.hand.model.IHandModel
import game.doppelkopf.domain.round.model.IRoundModel

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