package game.doppelkopf.domain.hand.ports.actions

import game.doppelkopf.common.port.IAction
import java.util.*

sealed interface IHandAction : IAction {
    /**
     * The [UUID] of the hand that this command should be applied to.
     */
    val handId: UUID
}