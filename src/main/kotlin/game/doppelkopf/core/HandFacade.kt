package game.doppelkopf.core

import game.doppelkopf.core.errors.ForbiddenActionException
import game.doppelkopf.core.play.enums.BiddingOption
import game.doppelkopf.core.play.enums.DeclarationOption
import game.doppelkopf.core.play.model.HandModelFactory
import game.doppelkopf.persistence.EntityNotFoundException
import game.doppelkopf.persistence.play.HandEntity
import game.doppelkopf.persistence.play.HandRepository
import game.doppelkopf.persistence.user.UserEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class HandFacade(
    private val roundFacade: RoundFacade,
    private val handRepository: HandRepository,
    private val handModelFactory: HandModelFactory
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

    fun declare(handId: UUID, declarationOption: DeclarationOption, user: UserEntity): HandEntity {
        val entity = load(handId, user)
        val hand = handModelFactory.create(entity)

        hand.declare(declarationOption)

        return entity
    }

    fun bid(handId: UUID, biddingOption: BiddingOption, user: UserEntity): HandEntity {
        val entity = load(handId, user)
        val hand = handModelFactory.create(entity)

        hand.bid(biddingOption)

        return entity
    }
}