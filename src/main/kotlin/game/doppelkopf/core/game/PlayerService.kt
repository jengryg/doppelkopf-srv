package game.doppelkopf.core.game

import game.doppelkopf.api.game.dto.PlayerCreateDto
import game.doppelkopf.core.errors.InvalidActionException
import game.doppelkopf.persistence.EntityNotFoundException
import game.doppelkopf.persistence.game.GameEntity
import game.doppelkopf.persistence.game.GameRepository
import game.doppelkopf.persistence.game.PlayerEntity
import game.doppelkopf.persistence.game.PlayerRepository
import game.doppelkopf.persistence.user.UserEntity
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class PlayerService(
    private val gameRepository: GameRepository,
    private val playerRepository: PlayerRepository
) {
    fun list(gameId: UUID): List<PlayerEntity> {
        return gameRepository.findByIdOrNull(gameId)?.players?.toList()
            ?: throw EntityNotFoundException.forEntity<GameEntity>(gameId)
    }

    fun load(playerId: UUID): PlayerEntity {
        return playerRepository.findByIdOrNull(playerId)
            ?: throw EntityNotFoundException.forEntity<PlayerEntity>(playerId)
    }

    /**
     * A player is crated when [user] joins the game specified by [playerCreateDto] at the given seat position.
     */
    @Transactional
    fun create(gameId: UUID, playerCreateDto: PlayerCreateDto, user: UserEntity): PlayerEntity {
        val game = gameRepository.findByIdOrNull(gameId)
            ?: throw EntityNotFoundException.forEntity<GameEntity>(gameId)

        val validatedSeat = canJoin(game, user, playerCreateDto.seat).getOrThrow()

        val playerEntity = PlayerEntity(
            user = user,
            game = game,
            seat = validatedSeat
        )

        game.players.add(playerEntity)

        return playerRepository.save(playerEntity)
    }

    /**
     * A user can join a game if the following conditions are satisfied:
     * - the game is in [GameState.INITIALIZED]
     * - the user has not joined the game already
     * - the seat the user wants to take is free
     * - the game player limit is not reached already
     */
    private fun canJoin(game: GameEntity, user: UserEntity, seat: Int): Result<Int> {
        if (game.state != GameState.INITIALIZED) {
            return Result.failure(
                InvalidActionException("Game:Join", "You can not join a game that has already started.")
            )
        }

        if (game.players.any { it.user == user }) {
            return Result.failure(
                InvalidActionException("Game:Join", "You already joined this game.")
            )
        }

        if (game.players.any { it.seat == seat }) {
            return Result.failure(
                InvalidActionException("Game:Join", "The seat you have chosen is already taken by another player.")
            )
        }

        if (game.players.count() >= game.maxNumberOfPlayers) {
            return Result.failure(
                InvalidActionException("Game:Join", "This game is already at its maximum capacity.")
            )
        }

        return Result.success(seat)
    }
}