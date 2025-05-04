package game.doppelkopf.core

import game.doppelkopf.adapter.persistence.model.game.GamePersistence
import game.doppelkopf.adapter.persistence.model.hand.HandRepository
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.round.RoundPersistence
import game.doppelkopf.adapter.persistence.model.round.RoundRepository
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.game.handler.GameDealModel
import game.doppelkopf.core.model.round.handler.RoundBidsEvaluationModel
import game.doppelkopf.core.model.round.handler.RoundDeclarationEvaluationModel
import game.doppelkopf.core.model.round.handler.RoundMarriageResolverModel
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*

@Service
class RoundFacade(
    private val roundRepository: RoundRepository,
    private val handRepository: HandRepository,
    private val gamePersistence: GamePersistence,
    private val roundPersistence: RoundPersistence,
) {
    /**
     * Start a new round in the game with [gameId] as [user].
     *
     * @param gameId the [UUID] of the game to start
     * @param user the user that want to start the round, must be the dealer
     *
     * @return the [RoundEntity] created
     */
    @Transactional
    fun create(gameId: UUID, user: UserEntity): RoundEntity {
        val game = gamePersistence.load(gameId)

        val mfp = ModelFactoryProvider()

        return GameDealModel(game, mfp).deal(
            mfp.user.create(user)
        ).let { (round, hands) ->
            roundRepository.save(round.entity).also {
                handRepository.saveAll(hands.toList().map { it.entity })
            }
        }
    }

    @Transactional
    fun evaluateDeclarations(roundId: UUID): RoundEntity {
        val round = roundPersistence.load(roundId)

        val mfp = ModelFactoryProvider()

        RoundDeclarationEvaluationModel(round, mfp).evaluateDeclarations()

        return round
    }

    @Transactional
    fun evaluateBids(roundId: UUID): RoundEntity {
        val round = roundPersistence.load(roundId)

        val mfp = ModelFactoryProvider()

        RoundBidsEvaluationModel(round, mfp).evaluateBids()

        return round
    }

    @Transactional
    fun resolveMarriage(roundId: UUID): RoundEntity {
        val round = roundPersistence.load(roundId)

        val mfp = ModelFactoryProvider()

        RoundMarriageResolverModel(round, mfp).resolveMarriage()

        return round
    }
}