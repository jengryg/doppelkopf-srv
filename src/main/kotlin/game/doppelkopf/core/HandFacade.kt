package game.doppelkopf.core

import game.doppelkopf.core.common.errors.ForbiddenActionException
import game.doppelkopf.core.common.enums.BiddingOption
import game.doppelkopf.core.common.enums.DeclarationOption
import game.doppelkopf.core.play.model.HandModelFactory
import game.doppelkopf.core.play.processor.BiddingProcessor
import game.doppelkopf.core.play.processor.DeclarationProcessor
import game.doppelkopf.persistence.errors.EntityNotFoundException
import game.doppelkopf.persistence.model.hand.HandEntity
import game.doppelkopf.persistence.model.hand.HandRepository
import game.doppelkopf.persistence.model.user.UserEntity
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class HandFacade(
    private val roundFacade: RoundFacade,
    private val handRepository: HandRepository,
    private val handModelFactory: HandModelFactory,
) {
    fun list(roundId: UUID): List<HandEntity> {
        return roundFacade.load(roundId).hands.toList()
    }

    fun load(handId: UUID, user: UserEntity): HandEntity {
        val hand = handRepository.findByIdOrNull(handId)
            ?: throw EntityNotFoundException.forEntity<HandEntity>(handId)

        if (hand.player.user != user) {
            throw ForbiddenActionException(
                "Hand:Show",
                "Only the player holding this hand can show its detailed information."
            )
        }

        return hand
    }

    @Transactional
    fun declare(handId: UUID, declarationOption: DeclarationOption, user: UserEntity): HandEntity {
        val entity = load(handId, user)
        val hand = handModelFactory.create(entity)

        hand.declare(declarationOption)

        // Try to run the processor, ignore if not ready.
        DeclarationProcessor.createWhenReady(entity.round).onSuccess {
            it.process()
        }

        return entity
    }

    @Transactional
    fun bid(handId: UUID, biddingOption: BiddingOption, user: UserEntity): HandEntity {
        val entity = load(handId, user)
        val hand = handModelFactory.create(entity)

        hand.bid(biddingOption)

        // Try to run the processor, ignore if not ready.
        BiddingProcessor.createWhenReady(entity.round).onSuccess {
            it.process()
        }

        return entity
    }
}