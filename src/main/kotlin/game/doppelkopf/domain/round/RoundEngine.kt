package game.doppelkopf.domain.round

import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.round.RoundPersistence
import game.doppelkopf.adapter.persistence.model.trick.TrickPersistence
import game.doppelkopf.adapter.persistence.model.turn.TurnEntity
import game.doppelkopf.adapter.persistence.model.turn.TurnPersistence
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.round.ports.commands.*
import game.doppelkopf.domain.round.service.RoundBidsEvaluationModel
import game.doppelkopf.domain.round.service.RoundDeclarationEvaluationModel
import game.doppelkopf.domain.round.service.RoundMarriageResolverModel
import game.doppelkopf.domain.round.service.RoundPlayCardModel
import game.doppelkopf.domain.trick.service.TrickEvaluationModel
import game.doppelkopf.domain.user.model.IUserModel
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class RoundEngine(
    private val roundPersistence: RoundPersistence,
    private val trickPersistence: TrickPersistence,
    private val turnPersistence: TurnPersistence
) {
    @Transactional
    fun execute(command: RoundCommandEvaluateDeclarations): RoundEntity {
        val resources = prepareResources(command)

        RoundDeclarationEvaluationModel(
            entity = resources.round,
            factoryProvider = resources.mfp
        ).evaluateDeclarations()

        return resources.round
    }

    @Transactional
    fun execute(command: RoundCommandEvaluateBids): RoundEntity {
        val resources = prepareResources(command)

        RoundBidsEvaluationModel(
            entity = resources.round,
            factoryProvider = resources.mfp
        ).evaluateBids()

        return resources.round
    }

    @Transactional
    fun execute(command: RoundCommandResolveMarriage): RoundEntity {
        val resources = prepareResources(command)

        RoundMarriageResolverModel(
            entity = resources.round,
            factoryProvider = resources.mfp
        ).resolveMarriage()

        return resources.round
    }

    @Transactional
    fun execute(command: RoundCommandPlayCard): TurnEntity {
        val resources = prepareResources(command)

        val (trick, turn) = RoundPlayCardModel(
            entity = resources.round,
            factoryProvider = resources.mfp
        ).playCard(
            encodedCard = command.encodedCard,
            user = resources.user
        )

        // TODO: refactoring
        TrickEvaluationModel(
            entity = trick.entity,
            factoryProvider = resources.mfp
        ).apply {
            canEvaluateTrick().onSuccess {
                evaluateTrick()
            }
        }

        // TODO: refactoring
        RoundMarriageResolverModel(
            entity = resources.round,
            factoryProvider = resources.mfp
        ).apply {
            canResolveMarriage().onSuccess { resolveMarriage() }
        }

        trickPersistence.save(trick.entity)

        return turnPersistence.save(turn.entity)
    }

    private fun prepareResources(command: IRoundCommand): RoundCommandResources {
        val mfp = ModelFactoryProvider()

        val round = roundPersistence.load(command.roundId)
        val user = mfp.user.create(command.user.entity)

        return RoundCommandResources(
            user = user,
            round = round,
            mfp = mfp
        )
    }

    private inner class RoundCommandResources(
        val user: IUserModel,
        val round: RoundEntity,
        val mfp: ModelFactoryProvider
    )
}