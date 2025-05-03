package game.doppelkopf.core

import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.trick.handler.TrickEvaluationModel
import game.doppelkopf.adapter.persistence.errors.EntityNotFoundException
import game.doppelkopf.adapter.persistence.model.trick.TrickEntity
import game.doppelkopf.adapter.persistence.model.trick.TrickRepository
import jakarta.transaction.Transactional
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

    @Transactional
    fun evaluateTrick(trickId: UUID): TrickEntity {
        val trick = load(trickId)

        val mfp = ModelFactoryProvider()

        TrickEvaluationModel(trick, mfp).evaluateTrick()

        return trick
    }
}