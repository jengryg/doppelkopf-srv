package game.doppelkopf.adapter.persistence.model.round

import game.doppelkopf.adapter.persistence.errors.EntityNotFoundException
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.game.GameRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class RoundPersistence(
    private val gameRepository: GameRepository,
    private val roundRepository: RoundRepository
) {
    fun load(id: UUID): RoundEntity {
        return roundRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException.forEntity<RoundEntity>(id)
    }

    fun listForGame(gameId: UUID): List<RoundEntity> {
        return gameRepository.findByIdOrNull(gameId)?.rounds?.toList()
            ?: throw EntityNotFoundException.forEntity<GameEntity>(gameId)
    }

    fun save(entity: RoundEntity): RoundEntity {
        return roundRepository.save(entity)
    }
}