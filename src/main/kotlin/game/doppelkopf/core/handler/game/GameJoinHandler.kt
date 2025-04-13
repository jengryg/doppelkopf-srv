package game.doppelkopf.core.handler.game

import game.doppelkopf.core.common.enums.GameState
import game.doppelkopf.core.common.errors.ofInvalidAction
import game.doppelkopf.core.model.game.GameModel
import game.doppelkopf.core.model.player.PlayerModel
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.persistence.model.player.PlayerEntity
import org.springframework.lang.CheckReturnValue

class GameJoinHandler(
    val game: GameModel,
    val user: UserModel
) {
    fun doHandle(seat: Int): PlayerEntity {
        canHandle(seat).getOrThrow()

        return PlayerEntity(
            user = user.entity,
            game = game.entity,
            seat = seat
        ).let { PlayerModel(it) }.also {
            game.addPlayer(it)
        }.entity
    }

    @CheckReturnValue
    fun canHandle(seat: Int): Result<Unit> {
        return when {
            game.state != GameState.INITIALIZED -> Result.ofInvalidAction(
                "Game:Join",
                "You can not join a game that has already started."
            )

            game.players.size >= game.maxNumberOfPlayers -> Result.ofInvalidAction(
                "Game:Join",
                "This game is already at its maximum capacity."
            )

            game.players.any { it.user.id == user.id } -> Result.ofInvalidAction(
                "Game:Join",
                "You already joined this game."
            )

            game.players.any { it.seat == seat } -> Result.ofInvalidAction(
                "Game:Join",
                "The seat you have chosen is already taken by another player."
            )

            else -> Result.success(Unit)
        }
    }
}