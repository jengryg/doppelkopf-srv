package game.doppelkopf.adapter.persistence.model.hand

import game.doppelkopf.adapter.persistence.errors.EntityNotFoundException
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.round.RoundRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class HandPersistence(
    private val roundRepository: RoundRepository,
    private val handRepository: HandRepository
) {
    fun load(id: UUID): HandEntity {
        return handRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException.forEntity<HandEntity>(id)
    }

    fun listForRound(roundId: UUID): List<HandEntity> {
        return roundRepository.findByIdOrNull(roundId)?.hands?.toList()
            ?: throw EntityNotFoundException.forEntity<RoundEntity>(roundId)
    }
}