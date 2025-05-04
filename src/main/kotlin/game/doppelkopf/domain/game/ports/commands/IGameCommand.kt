package game.doppelkopf.domain.game.ports.commands

import game.doppelkopf.common.port.commands.ICommand
import java.util.UUID

sealed interface IGameCommand : ICommand {
    /**
     * The [UUID] of the game that this command should be applied to.
     */
    val gameId: UUID
}