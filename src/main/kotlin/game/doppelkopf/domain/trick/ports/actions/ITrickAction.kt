package game.doppelkopf.domain.trick.ports.actions

import game.doppelkopf.common.port.IAction
import java.util.*

sealed interface ITrickAction : IAction {
    /**
     * The [UUID] of the trick that this command should be applied to.
     */
    val trickId: UUID
}