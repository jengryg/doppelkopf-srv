package game.doppelkopf.adapter.persistence.model.player

import game.doppelkopf.adapter.persistence.errors.EntityNotFoundException
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.game.GameRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class PlayerPersistence(
    private val gameRepository: GameRepository,
    private val playerRepository: PlayerRepository
) {
    fun load(id: UUID): PlayerEntity {
        return playerRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException.forEntity<PlayerEntity>(id)
    }

    fun listForGame(gameId: UUID): List<PlayerEntity> {
        return gameRepository.findByIdOrNull(gameId)?.players?.toList()
            ?: throw EntityNotFoundException.forEntity<GameEntity>(gameId)
    }

    fun save(entity: PlayerEntity): PlayerEntity {
        return playerRepository.save(entity)
    }
}