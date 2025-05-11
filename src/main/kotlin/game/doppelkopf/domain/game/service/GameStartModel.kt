package game.doppelkopf.domain.game.service

import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.common.errors.ofForbiddenAction
import game.doppelkopf.common.errors.ofInvalidAction
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.game.enums.GameState
import game.doppelkopf.domain.game.model.GameModelAbstract
import game.doppelkopf.domain.user.model.IUserModel
import org.springframework.lang.CheckReturnValue
import java.security.SecureRandom
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

        val dealerIndex = SecureRandom.getInstance("SHA1PRNG").apply {
            setSeed(seed)
        }.nextInt(0, players.size)

        players.values.sortedBy { it.seat }[dealerIndex].dealer = true

        started = Instant.now()
        state = GameState.WAITING_FOR_DEAL
    }

    /**
     * Check if all conditions for [user] to start the game are satisfied.
     */
    @CheckReturnValue
    fun canStart(user: IUserModel): Result<Unit> {
        return when {
            creator != user -> Result.ofForbiddenAction("Only the creator of the game can start it.")
            state != GameState.INITIALIZED -> Result.ofInvalidAction("The game has been started already.")
            players.size < 4 -> Result.ofInvalidAction("The game needs to have at least 4 players.")

            else -> Result.success(Unit)
        }
    }
}