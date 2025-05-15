package game.doppelkopf.domain.hand

import game.doppelkopf.adapter.persistence.model.call.CallEntity
import game.doppelkopf.adapter.persistence.model.call.CallPersistence
import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.hand.ports.commands.HandCommandBid
import game.doppelkopf.domain.hand.ports.commands.HandCommandCall
import game.doppelkopf.domain.hand.ports.commands.HandCommandDeclare
import game.doppelkopf.domain.hand.service.HandBiddingModel
import game.doppelkopf.domain.hand.service.HandCallModel
import game.doppelkopf.domain.hand.service.HandDeclareModel
import game.doppelkopf.domain.round.service.RoundBidsEvaluationModel
import game.doppelkopf.domain.round.service.RoundDeclarationEvaluationModel
import org.springframework.stereotype.Service

@Service
class HandEngine(
    private val callPersistence: CallPersistence
) {

    fun execute(command: HandCommandDeclare): HandEntity {
        val mfp = ModelFactoryProvider()

        HandDeclareModel(
            entity = command.hand,
            factoryProvider = mfp
        ).declare(
            user = mfp.user.create(command.user),
            declarationOption = command.declarationOption
        )

        // TODO: refactoring
        RoundDeclarationEvaluationModel(
            entity = command.hand.round,
            factoryProvider = mfp
        ).apply {
            canEvaluateDeclarations().onSuccess { evaluateDeclarations() }
        }

        return command.hand
    }

    fun execute(command: HandCommandBid): HandEntity {
        val mfp = ModelFactoryProvider()

        HandBiddingModel(
            entity = command.hand,
            factoryProvider = mfp
        ).bid(
            user = mfp.user.create(command.user),
            biddingOption = command.biddingOption
        )

        // TODO: refactoring
        RoundBidsEvaluationModel(
            entity = command.hand.round,
            factoryProvider = mfp
        ).apply {
            canEvaluateBids().onSuccess { evaluateBids() }
        }

        return command.hand
    }

    fun execute(command: HandCommandCall): CallEntity {
        val mfp = ModelFactoryProvider()

        val call = HandCallModel(
            entity = command.hand,
            factoryProvider = mfp
        ).makeCall(
            callType = command.callType,
            user = mfp.user.create(command.user),
        )

        return callPersistence.save(call.entity)
    }
}