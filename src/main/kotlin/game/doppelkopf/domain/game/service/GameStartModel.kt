package game.doppelkopf.domain.game.service

import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.domain.game.enums.GameState
import game.doppelkopf.common.errors.ofForbiddenAction
import game.doppelkopf.common.errors.ofInvalidAction
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.game.model.GameModelAbstract
import game.doppelkopf.domain.user.model.IUserModel
import org.springframework.lang.CheckReturnValue
import java.time.Instant

class GameStartModel(
    entity: GameEntity,
    factoryProvider: ModelFactoryProvider
) : GameModelAbstract(entity, factoryProvider) {
    /**
     * Start the game by marking the [started] time and transition the [state] of the game to
     * [GameState.WAITING_FOR_DEAL]. Choose one of the [players] randomly as first dealer of the game.
     */
    fun start(user: IUserModel) {
        canStart(user).getOrThrow()
        // the first dealer is decided randomly
        players.values.forEach { it.dealer = false }
        // TODO: seeded randomness for seed based games
        players.values.random().dealer = true

        started = Instant.now()
        state = GameState.WAITING_FOR_DEAL
    }

    /**
     * Check if all conditions for [user] to start the game are satisfied.
     */
    @CheckReturnValue
    fun canStart(user: IUserModel): Result<Unit> {
        return when {
            creator != user -> forbidden("Only the creator of the game can start it.")
            state != GameState.INITIALIZED -> invalid("The game has been started already.")
            players.size < 4 -> invalid("The game needs to have at least 4 players.")

            else -> Result.success(Unit)
        }
    }

    companion object {
        const val ACTION = "Game:Start"

        fun forbidden(reason: String): Result<Unit> {
            return Result.ofForbiddenAction(ACTION, reason)
        }

        fun invalid(reason: String): Result<Unit> {
            return Result.ofInvalidAction(ACTION, reason)
        }
    }
}