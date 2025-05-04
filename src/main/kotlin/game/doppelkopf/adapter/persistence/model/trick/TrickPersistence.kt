package game.doppelkopf.adapter.persistence.model.trick

import game.doppelkopf.adapter.persistence.errors.EntityNotFoundException
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.round.RoundRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class TrickPersistence(
    private val trickRepository: TrickRepository,
    private val roundRepository: RoundRepository
) {
    fun load(id: UUID): TrickEntity {
        return trickRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException.forEntity<TrickEntity>(id)
    }

    fun listForRound(roundId: UUID): List<TrickEntity> {
        return roundRepository.findByIdOrNull(roundId)?.tricks?.toList()
            ?: throw EntityNotFoundException.forEntity<RoundEntity>(roundId)
    }

    fun save(entity: TrickEntity): TrickEntity {
        return trickRepository.save(entity)
    }
}