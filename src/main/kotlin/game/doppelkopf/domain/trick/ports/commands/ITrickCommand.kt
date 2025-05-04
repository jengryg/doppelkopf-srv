package game.doppelkopf.domain.trick.ports.commands

import game.doppelkopf.common.port.commands.ICommand
import java.util.*

sealed interface ITrickCommand : ICommand {
    /**
     * The [UUID] of the trick that this command should be applied to.
     */
    val trickId: UUID
}