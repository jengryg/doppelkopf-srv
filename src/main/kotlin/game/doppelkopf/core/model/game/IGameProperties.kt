package game.doppelkopf.core.model.game

import game.doppelkopf.core.common.IBaseProperties
import game.doppelkopf.core.common.IStartedEnded
import game.doppelkopf.core.common.enums.GameState

interface IGameProperties : IBaseProperties, IStartedEnded {
    /**
     * How many players are at max allowed to join this game.
     */
    val maxNumberOfPlayers: Int

    /**
     * Current [GameState] of this game.
     */
    val state: GameState
}