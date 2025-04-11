package game.doppelkopf.core.handler

import game.doppelkopf.api.game.dto.PlayerCreateDto
import game.doppelkopf.core.game.model.GameModelFactory
import game.doppelkopf.persistence.errors.EntityNotFoundException
import game.doppelkopf.persistence.model.game.GameEntity
import game.doppelkopf.persistence.model.game.GameRepository
import game.doppelkopf.persistence.model.player.PlayerEntity
import game.doppelkopf.persistence.model.player.PlayerRepository
import game.doppelkopf.persistence.model.user.UserEntity
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class PlayerFacade(
    private val gameRepository: GameRepository,
    private val playerRepository: PlayerRepository,
    private val gameModelFactory: GameModelFactory,
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

        return gameModelFactory.create(game).join(user, playerCreateDto.seat).let {
            playerRepository.save(it)
        }
    }
}