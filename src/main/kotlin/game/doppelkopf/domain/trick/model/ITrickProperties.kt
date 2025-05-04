package game.doppelkopf.domain.trick.model

import game.doppelkopf.common.IBaseProperties
import game.doppelkopf.common.IStartedEnded
import game.doppelkopf.domain.cards.CardDemand
import game.doppelkopf.core.common.enums.TrickState

interface ITrickProperties : IBaseProperties, IStartedEnded {
    /**
     * The number of this trick with respect to the round.
     */
    val number: Int

    /**
     * The index of the hand that played the first card.
     * This is used to determine the player turn order.
     */
    val openIndex: Int

    /**
     * The first card of the trick will determine the demand for all other cards to be played.
     */
    val demand: CardDemand

    /**
     * The index of the card in the trick that is currently the highest one played.
     * If the trick is completed, the leading card determines the winner of the trick.
     */
    var leadingCardIndex: Int

    /**
     * The score of the trick, i.e. the sum of the value of the cards played into the trick.
     */
    var score: Int

    /**
     * The current state of this trick.
     */
    var state: TrickState
}