package game.doppelkopf.domain.lobby

import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.game.GamePersistence
import game.doppelkopf.domain.game.GameEngine
import game.doppelkopf.domain.game.ports.commands.GameCommandJoinAsPlayer
import game.doppelkopf.domain.lobby.ports.commands.LobbyCommandCreateNewGame
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.security.SecureRandom

@Service
class LobbyEngine(
    private val gamePersistence: GamePersistence,
    @Lazy
    private val gameEngine: GameEngine
) {
    fun execute(command: LobbyCommandCreateNewGame): GameEntity {
        // Initialize the seed if not set in command.
        val seed = command.seed
            ?: SecureRandom.getInstance("SHA1PRNG").generateSeed(256)

        val game = GameEntity(
            creator = command.user,
            seed = seed,
            maxNumberOfPlayers = command.playerLimit,
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