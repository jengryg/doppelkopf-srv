package game.doppelkopf.core

import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.adapter.persistence.model.hand.HandPersistence
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.domain.hand.enums.BiddingOption
import game.doppelkopf.domain.hand.enums.DeclarationOption
import game.doppelkopf.core.errors.ForbiddenActionException
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.hand.service.HandBiddingModel
import game.doppelkopf.domain.hand.service.HandDeclareModel
import game.doppelkopf.domain.round.service.RoundBidsEvaluationModel
import game.doppelkopf.domain.round.service.RoundDeclarationEvaluationModel
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*

@Service
class HandFacade(
    private val handPersistence: HandPersistence
) {
    fun show(handId: UUID, user: UserEntity): HandEntity {
        val hand = handPersistence.load(handId)

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
        val hand = handPersistence.load(handId)

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
        val hand = handPersistence.load(handId)

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