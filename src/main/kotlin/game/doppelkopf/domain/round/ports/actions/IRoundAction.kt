package game.doppelkopf.domain.round.ports.actions

import game.doppelkopf.common.port.IAction
import java.util.*

sealed interface IRoundAction : IAction {
    /**
     * The [UUID] of the round that this command should be applied to.
     */
    val roundId: UUID
}