package game.doppelkopf.adapter.persistence.model.hand

import game.doppelkopf.adapter.persistence.errors.EntityNotFoundException
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.round.RoundRepository
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.common.errors.ForbiddenActionException
import game.doppelkopf.utils.Quadruple
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

    fun loadForUser(handId: UUID, user: UserEntity): HandEntity {
        // TODO maybe refactor or change this here?
        return load(handId).takeIf { it.player.user == user }
            ?: throw ForbiddenActionException(
                "Hand:Show",
                "Only the player holding this hand can show its detailed information."
            )
    }

    fun listForRound(roundId: UUID): List<HandEntity> {
        return roundRepository.findByIdOrNull(roundId)?.hands?.toList()
            ?: throw EntityNotFoundException.forEntity<RoundEntity>(roundId)
    }

    fun saveAll(entities: Quadruple<HandEntity>): List<HandEntity> {
        return handRepository.saveAll(entities.toList())
    }
}