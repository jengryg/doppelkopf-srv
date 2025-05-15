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
import game.doppelkopf.domain.round.RoundEngine
import game.doppelkopf.domain.round.ports.commands.RoundCommandEvaluateBids
import game.doppelkopf.domain.round.ports.commands.RoundCommandEvaluateDeclarations
import game.doppelkopf.domain.round.ports.commands.RoundCommandEvaluateTeamReveal
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
class HandEngine(
    private val callPersistence: CallPersistence,
    @Lazy
    private val roundEngine: RoundEngine
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

        // TODO: the engine execute methods are throwing the occurring exceptions, thus we need to silence them for now
        //  later this should be changed to a more resilient implementation where the engines are not throwing
        runCatching {
            roundEngine.execute(
                command = RoundCommandEvaluateDeclarations(
                    round = command.hand.round
                )
            )
            // just ignore the exceptions here
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

        // TODO: the engine execute methods are throwing the occurring exceptions, thus we need to silence them for now
        //  later this should be changed to a more resilient implementation where the engines are not throwing
        runCatching {
            roundEngine.execute(
                command = RoundCommandEvaluateBids(
                    round = command.hand.round
                )
            )
            // just ignore the exceptions here
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

        // TODO: the engine execute methods are throwing the occurring exceptions, thus we need to silence them for now
        //  later this should be changed to a more resilient implementation where the engines are not throwing
        runCatching {
            roundEngine.execute(
                command = RoundCommandEvaluateTeamReveal(
                    round = command.hand.round,
                )
            )
            // just ignore the exceptions here
        }

        return callPersistence.save(call.entity)
    }
}