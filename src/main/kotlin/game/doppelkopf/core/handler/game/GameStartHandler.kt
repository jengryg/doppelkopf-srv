package game.doppelkopf.core.handler.game

import game.doppelkopf.core.common.enums.GameState
import game.doppelkopf.core.common.errors.ofForbiddenAction
import game.doppelkopf.core.common.errors.ofInvalidAction
import game.doppelkopf.core.model.game.GameModel
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.persistence.model.game.GameEntity
import org.springframework.lang.CheckReturnValue

class GameStartHandler(
    val game: GameModel,
    val user: UserModel
) {
    fun doHandle(): GameEntity {
        canHandle().getOrThrow()
        game.start()

        return game.entity
    }

    @CheckReturnValue
    fun canHandle(): Result<Unit> {
        return when {
            game.creator.id != user.id -> Result.ofForbiddenAction(
                "Game:Start",
                "Only the creator of the game can start it."
            )

            game.state != GameState.INITIALIZED -> Result.ofInvalidAction(
                "Game:Start",
                "The game has been started already."
            )

            game.players.size < 4 -> Result.ofInvalidAction(
                "Game:Start",
                "The game needs to have at least 4 players."
            )

            else -> Result.success(Unit)
        }
    }
}