package game.doppelkopf.core.model.round

import game.doppelkopf.core.cards.DeckMode
import game.doppelkopf.core.common.IBaseProperties
import game.doppelkopf.core.common.IStartedEnded
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
    val state: RoundState

    /**
     * Current [RoundContract] of this round.
     */
    val contract: RoundContract

    /**
     * Current [DeckMode] of this round.
     */
    val deck: DeckMode
}