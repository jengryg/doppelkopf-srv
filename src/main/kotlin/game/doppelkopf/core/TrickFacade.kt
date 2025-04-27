package game.doppelkopf.core

import game.doppelkopf.persistence.errors.EntityNotFoundException
import game.doppelkopf.persistence.model.trick.TrickEntity
import game.doppelkopf.persistence.model.trick.TrickRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class TrickFacade(
    private val roundFacade: RoundFacade,
    private val trickRepository: TrickRepository
) {
    fun list(roundId: UUID): List<TrickEntity> {
        return roundFacade.load(roundId).tricks.toList()
    }

    fun load(trickId: UUID): TrickEntity {
        return trickRepository.findByIdOrNull(trickId)
            ?: throw EntityNotFoundException.forEntity<TrickEntity>(trickId)
    }
}