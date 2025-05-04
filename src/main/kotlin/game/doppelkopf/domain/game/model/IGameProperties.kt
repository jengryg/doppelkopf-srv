package game.doppelkopf.domain.game.model

import game.doppelkopf.common.model.IBaseProperties
import game.doppelkopf.common.model.IStartedEnded
import game.doppelkopf.domain.game.enums.GameState

interface IGameProperties : IBaseProperties, IStartedEnded {
    /**
     * How many players are at max allowed to join this game.
     */
    val maxNumberOfPlayers: Int

    /**
     * Current [GameState] of this game.
     */
    var state: GameState
}