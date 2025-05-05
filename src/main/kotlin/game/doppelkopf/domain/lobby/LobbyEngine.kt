package game.doppelkopf.domain.lobby

import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.game.GamePersistence
import game.doppelkopf.domain.game.GameEngine
import game.doppelkopf.domain.game.ports.commands.GameCommandJoinAsPlayer
import game.doppelkopf.domain.lobby.ports.commands.LobbyCommandCreateNewGame
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
class LobbyEngine(
    private val gamePersistence: GamePersistence,
    @Lazy
    private val gameEngine: GameEngine
) {
    fun execute(command: LobbyCommandCreateNewGame): GameEntity {
        val game = GameEntity(
            creator = command.user,
            maxNumberOfPlayers = command.playerLimit
        ).let { gamePersistence.save(it) }

        gameEngine.execute(
            command = GameCommandJoinAsPlayer(
                user = command.user,
                game = game,
                seat = 0
            )
        )

        return game
    }
}