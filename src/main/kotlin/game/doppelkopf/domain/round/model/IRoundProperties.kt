package game.doppelkopf.domain.round.model

import game.doppelkopf.common.model.IBaseProperties
import game.doppelkopf.common.model.IStartedEnded
import game.doppelkopf.domain.deck.enums.DeckMode
import game.doppelkopf.domain.round.enums.RoundContract
import game.doppelkopf.domain.round.enums.RoundState
import game.doppelkopf.domain.round.enums.RoundWinner

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

    /**
     * Current [RoundWinner] of this round.
     */
    var winner: RoundWinner
}