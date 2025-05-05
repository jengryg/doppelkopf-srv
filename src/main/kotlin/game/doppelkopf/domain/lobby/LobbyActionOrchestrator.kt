package game.doppelkopf.domain.lobby

import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.domain.lobby.ports.actions.LobbyActionCreateNewGame
import game.doppelkopf.domain.lobby.ports.commands.LobbyCommandCreateNewGame
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class LobbyActionOrchestrator(
    private val lobbyEngine: LobbyEngine
) {
    @Transactional
    fun execute(action: LobbyActionCreateNewGame): GameEntity {
        val command = LobbyCommandCreateNewGame(
            user = action.user.entity,
            playerLimit = action.playerLimit
        )

        return lobbyEngine.execute(command)
    }
}