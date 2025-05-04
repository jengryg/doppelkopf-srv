package game.doppelkopf.core.model.game.handler

import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.core.common.enums.GameState
import game.doppelkopf.core.errors.ofInvalidAction
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.game.GameModelAbstract
import game.doppelkopf.core.model.player.IPlayerModel
import game.doppelkopf.core.model.user.IUserModel
import org.springframework.lang.CheckReturnValue

class GameJoinModel(
    entity: GameEntity,
    factoryProvider: ModelFactoryProvider
) : GameModelAbstract(entity, factoryProvider) {
    /**
     * Join the game as [user] on [seat] by creating a new player and adding it to this game.
     */
    fun join(user: IUserModel, seat: Int): IPlayerModel {
        canJoin(user, seat).getOrThrow()

        return factoryProvider.player.create(
            PlayerEntity(user = user.entity, game = entity, seat = seat)
        ).also {
            addPlayer(it)
        }
    }

    /**
     * Check if all conditions for [user] to join the game on [seat] are satisfied.
     */
    @CheckReturnValue
    fun canJoin(user: IUserModel, seat: Int): Result<Unit> {
        return when {
            state != GameState.INITIALIZED -> invalid("You can not join a game that has already started.")
            players.size >= maxNumberOfPlayers -> invalid("This game is already at its maximum capacity.")
            players[user] != null -> invalid("You already joined this game.")
            players.values.any { it.seat == seat } -> invalid("The seat you have chosen is already taken by another player.")

            else -> Result.success(Unit)
        }
    }

    companion object {
        const val ACTION = "Game:Join"

        fun invalid(reason: String): Result<Unit> {
            return Result.ofInvalidAction(ACTION, reason)
        }
    }
}