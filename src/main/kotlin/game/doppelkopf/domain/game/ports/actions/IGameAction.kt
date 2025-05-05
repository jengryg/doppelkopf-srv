package game.doppelkopf.domain.game.ports.actions

import game.doppelkopf.common.port.IAction
import java.util.UUID

sealed interface IGameAction : IAction {
    /**
     * The [UUID] of the game that this command should be applied to.
     */
    val gameId: UUID
}