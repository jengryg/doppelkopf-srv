package game.doppelkopf.core.model.round

import game.doppelkopf.common.IBaseProperties
import game.doppelkopf.common.IStartedEnded
import game.doppelkopf.core.cards.DeckMode
import game.doppelkopf.core.common.enums.RoundContract
import game.doppelkopf.core.common.enums.RoundState

interface IRoundProperties : IBaseProperties, IStartedEnded {
    /**
     * The number of this round with respect to the game.
     */
    val number: Int

    /**
     * Current [RoundState] of this round.
     */
    var state: RoundState

    /**
     * Current [RoundContract] of this round.
     */
    var contract: RoundContract

    /**
     * Current [DeckMode] of this round.
     */
    var deckMode: DeckMode
}