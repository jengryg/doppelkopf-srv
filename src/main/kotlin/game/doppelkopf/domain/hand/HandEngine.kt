package game.doppelkopf.domain.hand

import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.adapter.persistence.model.hand.HandPersistence
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.hand.ports.commands.HandCommandBid
import game.doppelkopf.domain.hand.ports.commands.HandCommandDeclare
import game.doppelkopf.domain.hand.ports.commands.IHandCommand
import game.doppelkopf.domain.hand.service.HandBiddingModel
import game.doppelkopf.domain.hand.service.HandDeclareModel
import game.doppelkopf.domain.round.service.RoundBidsEvaluationModel
import game.doppelkopf.domain.round.service.RoundDeclarationEvaluationModel
import game.doppelkopf.domain.user.model.IUserModel
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class HandEngine(private val handPersistence: HandPersistence) {
    @Transactional
    fun execute(command: HandCommandDeclare): HandEntity {
        val resources = prepareResources(command)

        HandDeclareModel(
            entity = resources.hand,
            factoryProvider = resources.mfp
        ).declare(
            user = resources.user,
            declarationOption = command.declaration
        )

        // TODO: refactoring
        RoundDeclarationEvaluationModel(
            entity = resources.hand.round,
            factoryProvider = resources.mfp
        ).apply {
            canEvaluateDeclarations().onSuccess { evaluateDeclarations() }
        }

        return resources.hand
    }

    @Transactional
    fun execute(command: HandCommandBid): HandEntity {
        val resources = prepareResources(command)

        HandBiddingModel(
            entity = resources.hand,
            factoryProvider = resources.mfp
        ).bid(
            user = resources.user,
            biddingOption = command.bid
        )

        // TODO: refactoring
        RoundBidsEvaluationModel(
            entity = resources.hand.round,
            factoryProvider = resources.mfp
        ).apply {
            canEvaluateBids().onSuccess { evaluateBids() }
        }

        return resources.hand
    }

    private fun prepareResources(command: IHandCommand): HandCommandResources {
        val mfp = ModelFactoryProvider()

        val hand = handPersistence.load(command.handId)
        val user = mfp.user.create(command.user.entity)

        return HandCommandResources(
            user = user,
            hand = hand,
            mfp = mfp
        )
    }

    private inner class HandCommandResources(
        val user: IUserModel,
        val hand: HandEntity,
        val mfp: ModelFactoryProvider
    )
}