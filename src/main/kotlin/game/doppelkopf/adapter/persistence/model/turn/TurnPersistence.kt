package game.doppelkopf.adapter.persistence.model.turn

import game.doppelkopf.adapter.persistence.errors.EntityNotFoundException
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.round.RoundRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class TurnPersistence(
    private val roundRepository: RoundRepository,
    private val turnRepository: TurnRepository,
) {
    fun load(id: UUID): TurnEntity {
        return turnRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException.forEntity<TurnEntity>(id)
    }

    fun listForRound(roundId: UUID): List<TurnEntity> {
        return roundRepository.findByIdOrNull(roundId)?.turns?.toList()
            ?: throw EntityNotFoundException.forEntity<RoundEntity>(roundId)
    }
}