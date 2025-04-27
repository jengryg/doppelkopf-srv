package game.doppelkopf.core

import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.round.handler.RoundPlayCardModel
import game.doppelkopf.persistence.errors.EntityNotFoundException
import game.doppelkopf.persistence.model.trick.TrickRepository
import game.doppelkopf.persistence.model.turn.TurnEntity
import game.doppelkopf.persistence.model.turn.TurnRepository
import game.doppelkopf.persistence.model.user.UserEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class TurnFacade(
    private val turnRepository: TurnRepository,
    private val trickRepository: TrickRepository,
    private val roundFacade: RoundFacade,
) {
    fun list(roundId: UUID): List<TurnEntity> {
        return roundFacade.load(roundId).turns.toList()
    }

    fun load(turnId: UUID): TurnEntity {
        return turnRepository.findByIdOrNull(turnId)
            ?: throw EntityNotFoundException.forEntity<TurnEntity>(turnId)
    }

    fun create(roundId: UUID, encodedCard: String, user: UserEntity): TurnEntity {
        val round = roundFacade.load(roundId)

        val mfp = ModelFactoryProvider()

        val (trickEntity, turnEntity) = RoundPlayCardModel(round, mfp).playCard(
            encodedCard,
            mfp.user.create(user)
        ).let {
            Pair(
                it.first.entity,
                it.second.entity
            )
        }

        // TODO: Determine winner of trick when full, trigger marriage resolving.
        // TODO: Determine result of round when last trick played.
        trickRepository.save(trickEntity)

        return turnRepository.save(turnEntity)
    }
}