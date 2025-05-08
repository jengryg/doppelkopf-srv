package game.doppelkopf.domain.round

import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.trick.TrickPersistence
import game.doppelkopf.adapter.persistence.model.turn.TurnEntity
import game.doppelkopf.adapter.persistence.model.turn.TurnPersistence
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.round.ports.commands.*
import game.doppelkopf.domain.round.service.RoundBidsEvaluationModel
import game.doppelkopf.domain.round.service.RoundDeclarationEvaluationModel
import game.doppelkopf.domain.round.service.RoundMarriageResolverModel
import game.doppelkopf.domain.round.service.RoundPlayCardModel
import game.doppelkopf.domain.trick.TrickEngine
import game.doppelkopf.domain.trick.ports.commands.TrickCommandEvaluate
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
class RoundEngine(
    private val trickPersistence: TrickPersistence,
    private val turnPersistence: TurnPersistence,
    @Lazy
    private val trickEngine: TrickEngine
) {
    fun execute(command: RoundCommandEvaluateDeclarations): RoundEntity {
        val mfp = ModelFactoryProvider()

        RoundDeclarationEvaluationModel(
            entity = command.round,
            factoryProvider = mfp
        ).evaluateDeclarations()

        return command.round
    }

    fun execute(command: RoundCommandEvaluateBids): RoundEntity {
        val mfp = ModelFactoryProvider()

        RoundBidsEvaluationModel(
            entity = command.round,
            factoryProvider = mfp
        ).evaluateBids()

        return command.round
    }

    fun execute(command: RoundCommandResolveMarriage): RoundEntity {
        val mfp = ModelFactoryProvider()

        RoundMarriageResolverModel(
            entity = command.round,
            factoryProvider = mfp
        ).resolveMarriage()

        return command.round
    }

    fun execute(command: RoundCommandPlayCard): TurnEntity {
        val mfp = ModelFactoryProvider()

        val (trick, turn) = RoundPlayCardModel(
            entity = command.round,
            factoryProvider = mfp,
        ).playCard(
            card = command.card,
            user = mfp.user.create(command.user)
        )

        // TODO: the engine execute methods are throwing the occurring exceptions, thus we need to silence them for now
        //  later this should be changed to a more resilient implementation where the engines are not throwing
        runCatching {
            trickEngine.execute(
                command = TrickCommandEvaluate(
                    trick = trick.entity
                )
            )
        }

        trickPersistence.save(trick.entity)

        return turnPersistence.save(turn.entity)
    }

    fun execute(command: RoundCommandEvaluate): RoundEntity {
        val mfp = ModelFactoryProvider()



        return command.round
    }
}