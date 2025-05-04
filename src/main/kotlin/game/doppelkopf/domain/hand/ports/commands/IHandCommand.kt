package game.doppelkopf.domain.hand.ports.commands

import game.doppelkopf.common.port.commands.ICommand
import java.util.*

sealed interface IHandCommand : ICommand {
    /**
     * The [UUID] of the hand that this command should be applied to.
     */
    val handId: UUID
}