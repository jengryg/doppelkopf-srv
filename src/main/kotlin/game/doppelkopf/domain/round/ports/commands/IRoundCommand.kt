package game.doppelkopf.domain.round.ports.commands

import game.doppelkopf.common.port.commands.ICommand
import java.util.*

sealed interface IRoundCommand : ICommand {
    /**
     * The [UUID] of the round that this command should be applied to.
     */
    val roundId: UUID
}