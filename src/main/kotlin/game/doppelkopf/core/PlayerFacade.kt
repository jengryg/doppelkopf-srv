package game.doppelkopf.core

import game.doppelkopf.api.game.dto.PlayerCreateDto
import game.doppelkopf.core.handler.game.GameJoinHandler
import game.doppelkopf.core.model.game.GameModel
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.persistence.errors.EntityNotFoundException
import game.doppelkopf.persistence.model.player.PlayerEntity
import game.doppelkopf.persistence.model.player.PlayerRepository
import game.doppelkopf.persistence.model.user.UserEntity
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class PlayerFacade(
    private val gameFacade: GameFacade,
    private val playerRepository: PlayerRepository
) {
    fun list(gameId: UUID): List<PlayerEntity> {
        return gameFacade.load(gameId).players.toList()
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
        return GameJoinHandler(
            game = GameModel(gameFacade.load(gameId)),
            user = UserModel(user)
        ).doHandle(playerCreateDto.seat).let {
            playerRepository.save(it)
        }
    }
}