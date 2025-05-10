package game.doppelkopf.domain.round

import game.doppelkopf.adapter.persistence.model.result.ResultPersistence
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.trick.TrickPersistence
import game.doppelkopf.adapter.persistence.model.turn.TurnEntity
import game.doppelkopf.adapter.persistence.model.turn.TurnPersistence
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.round.ports.commands.*
import game.doppelkopf.domain.round.service.*
import game.doppelkopf.domain.trick.TrickEngine
import game.doppelkopf.domain.trick.enums.TrickState
import game.doppelkopf.domain.trick.ports.commands.TrickCommandEvaluate
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
class RoundEngine(
    private val trickPersistence: TrickPersistence,
    private val turnPersistence: TurnPersistence,
    @Lazy
    private val trickEngine: TrickEngine,
    private val resultPersistence: ResultPersistence
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

        if (trick.state == TrickState.FOURTH_CARD_PLAYED) {
            // Fourth card played implies the trick is complete and can be directly evaluated.
            trickEngine.execute(
                command = TrickCommandEvaluate(
                    trick = trick.entity
                )
            )
        }

        if (trick.state == TrickState.FIRST_CARD_PLAYED) {
            // we explicitly save only the freshly created trick
            trickPersistence.save(trick.entity)
        }

        return turnPersistence.save(turn.entity)
    }

    fun execute(command: RoundCommandEvaluate): RoundEntity {
        val mfp = ModelFactoryProvider()

        val results = RoundEvaluationModel(
            entity = command.round,
            factoryProvider = mfp,
        ).evaluateRound()

        resultPersistence.save(results.map { it.entity })

        return command.round
    }
}