package game.doppelkopf.adapter.persistence.model.game

import game.doppelkopf.adapter.persistence.errors.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class GamePersistence(
    private val gameRepository: GameRepository
) {
    fun load(id: UUID): GameEntity {
        return gameRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException.forEntity<GameEntity>(id)
    }

    fun list(): List<GameEntity> {
        return gameRepository.findAll()
    }

    fun save(entity: GameEntity): GameEntity {
        return gameRepository.save(entity)
    }
}