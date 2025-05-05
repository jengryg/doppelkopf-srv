package game.doppelkopf.domain.game

import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.game.GamePersistence
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.domain.game.ports.actions.GameActionDealNewRound
import game.doppelkopf.domain.game.ports.actions.GameActionJoinAsPlayer
import game.doppelkopf.domain.game.ports.actions.GameActionStartPlaying
import game.doppelkopf.domain.game.ports.commands.GameCommandDealNewRound
import game.doppelkopf.domain.game.ports.commands.GameCommandJoinAsPlayer
import game.doppelkopf.domain.game.ports.commands.GameCommandStartPlaying
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class GameActionOrchestrator(
    private val gamePersistence: GamePersistence,
    private val gameEngine: GameEngine
) {
    @Transactional
    fun execute(action: GameActionJoinAsPlayer): PlayerEntity {
        val command = GameCommandJoinAsPlayer(
            user = action.user.entity,
            game = gamePersistence.load(id = action.gameId),
            seat = action.seat
        )

        return gameEngine.execute(command)
    }

    @Transactional
    fun execute(action: GameActionStartPlaying): GameEntity {
        val command = GameCommandStartPlaying(
            user = action.user.entity,
            game = gamePersistence.load(id = action.gameId),
        )

        gameEngine.execute(command)

        return command.game
    }

    @Transactional
    fun execute(action: GameActionDealNewRound): RoundEntity {
        val command = GameCommandDealNewRound(
            user = action.user.entity,
            game = gamePersistence.load(id = action.gameId),
        )

        return gameEngine.execute(command)
    }
}