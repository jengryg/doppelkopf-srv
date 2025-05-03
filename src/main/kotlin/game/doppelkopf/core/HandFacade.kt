package game.doppelkopf.core

import game.doppelkopf.core.common.enums.BiddingOption
import game.doppelkopf.core.common.enums.DeclarationOption
import game.doppelkopf.core.errors.ForbiddenActionException
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.hand.handler.HandBiddingModel
import game.doppelkopf.core.model.hand.handler.HandDeclareModel
import game.doppelkopf.core.model.round.handler.RoundBidsEvaluationModel
import game.doppelkopf.core.model.round.handler.RoundDeclarationEvaluationModel
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
    private val handRepository: HandRepository
) {
    fun list(roundId: UUID): List<HandEntity> {
        return roundFacade.load(roundId).hands.toList()
    }

    private fun load(handId: UUID): HandEntity {
        return handRepository.findByIdOrNull(handId)
            ?: throw EntityNotFoundException.forEntity<HandEntity>(handId)
    }

    fun show(handId: UUID, user: UserEntity): HandEntity {
        val hand = load(handId)

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
        val hand = load(handId)

        val mfp = ModelFactoryProvider()

        HandDeclareModel(hand, mfp).declare(
            mfp.user.create(user), declarationOption
        ).also {
            val evaluator = RoundDeclarationEvaluationModel(hand.round, mfp)

            // Try to evaluate the declarations, ignore if not ready.
            evaluator.canEvaluateDeclarations().onSuccess {
                evaluator.evaluateDeclarations()
            }
        }

        return hand
    }

    @Transactional
    fun bid(handId: UUID, biddingOption: BiddingOption, user: UserEntity): HandEntity {
        val hand = load(handId)

        val mfp = ModelFactoryProvider()

        HandBiddingModel(hand, mfp).bid(
            mfp.user.create(user), biddingOption
        ).also {
            val evaluator = RoundBidsEvaluationModel(hand.round, mfp)

            // Try to evaluate the bids, ignore if not ready.
            evaluator.canEvaluateBids().onSuccess {
                evaluator.evaluateBids()
            }
        }

        return hand
    }
}