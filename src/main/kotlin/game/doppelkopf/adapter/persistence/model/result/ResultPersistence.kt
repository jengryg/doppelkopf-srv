package game.doppelkopf.adapter.persistence.model.result

import game.doppelkopf.adapter.persistence.errors.EntityNotFoundException
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.round.RoundRepository
import game.doppelkopf.utils.Teamed
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class ResultPersistence(
    private val resultRepository: ResultRepository,
    private val roundRepository: RoundRepository
) {
    fun load(id: UUID): ResultEntity {
        return resultRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException.forEntity<ResultEntity>(id)
    }

    fun loadForRound(roundId: UUID): Teamed<ResultEntity?> {
        val round = roundRepository.findByIdOrNull(roundId)
            ?: throw EntityNotFoundException.forEntity<RoundEntity>(roundId)

        return round.results.takeIf { it.size == 2 }?.let { res ->
            Teamed.from(res) { it.team.internal }
        } ?: Teamed(null, null)
    }

    fun save(entity: ResultEntity): ResultEntity {
        return resultRepository.save(entity)
    }

    fun save(teamedEntities: Teamed<ResultEntity>): Teamed<ResultEntity> {
        return teamedEntities.map { save(it) }
    }
}