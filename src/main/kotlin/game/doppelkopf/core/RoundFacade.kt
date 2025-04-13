package game.doppelkopf.core

import game.doppelkopf.core.handler.round.RoundDealHandler
import game.doppelkopf.core.model.game.GameModel
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.core.play.processor.BiddingProcessor
import game.doppelkopf.core.play.processor.DeclarationProcessor
import game.doppelkopf.persistence.errors.EntityNotFoundException
import game.doppelkopf.persistence.model.hand.HandRepository
import game.doppelkopf.persistence.model.round.RoundEntity
import game.doppelkopf.persistence.model.round.RoundRepository
import game.doppelkopf.persistence.model.user.UserEntity
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class RoundFacade(
    private val gameFacade: GameFacade,
    private val roundRepository: RoundRepository,
    private val handRepository: HandRepository,
) {
    fun list(gameId: UUID): List<RoundEntity> {
        return gameFacade.load(gameId).rounds.toList()
    }

    fun load(roundId: UUID): RoundEntity {
        return roundRepository.findByIdOrNull(roundId)
            ?: throw EntityNotFoundException.forEntity<RoundEntity>(roundId)
    }

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
        return RoundDealHandler(
            game = GameModel(gameFacade.load(gameId)),
            user = UserModel(user)
        ).doHandle().let { (round, hands) ->
            roundRepository.save(round).also {
                handRepository.saveAll(hands.toList())
            }
        }
    }

    @Transactional
    fun evaluateDeclarations(roundId: UUID): RoundEntity {
        val round = load(roundId)

        // Force the evaluation, thus we need to throw when not ready.
        DeclarationProcessor.createWhenReady(round).getOrThrow().process()

        return round
    }

    @Transactional
    fun evaluateBids(roundId: UUID): RoundEntity {
        val round = load(roundId)

        // Force the evaluation, thus we need to throw when not ready.
        BiddingProcessor.createWhenReady(round).getOrThrow().process()

        return round
    }
}