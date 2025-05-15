package game.doppelkopf.adapter.persistence.model.call

import game.doppelkopf.adapter.persistence.errors.EntityNotFoundException
import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.adapter.persistence.model.hand.HandRepository
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.round.RoundRepository
import game.doppelkopf.utils.Teamed
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class CallPersistence(
    private val handRepository: HandRepository,
    private val roundRepository: RoundRepository,
    private val callRepository: CallRepository,
) {
    fun load(id: UUID): CallEntity {
        return callRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException.forEntity<CallEntity>(id)
    }

    fun listForHand(handId: UUID): List<CallEntity> {
        return handRepository.findByIdOrNull(handId)?.calls?.toList()
            ?: throw EntityNotFoundException.forEntity<HandEntity>(handId)
    }

    fun teamedForRound(roundId: UUID): Teamed<List<CallEntity>> {
        return roundRepository.findByIdOrNull(roundId)?.let { round ->
            Teamed.filter(round.hands) { hand -> hand.internalTeam }.map { hands ->
                hands.flatMap { h -> h.calls }.sortedBy { h -> h.callType.orderIndex }
            }
        } ?: throw EntityNotFoundException.forEntity<RoundEntity>(roundId)
    }

    fun save(entity: CallEntity): CallEntity {
        return callRepository.save(entity)
    }
}