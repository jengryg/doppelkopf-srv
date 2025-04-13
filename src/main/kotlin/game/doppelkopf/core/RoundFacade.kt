package game.doppelkopf.core

import game.doppelkopf.core.game.model.GameModelFactory
import game.doppelkopf.core.play.model.RoundModelFactory
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
    private val gameModelFactory: GameModelFactory,
    private val roundModelFactory: RoundModelFactory,
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
     * A round is created when the [user] deals the cards to the players.
     */
    @Transactional
    fun create(gameId: UUID, user: UserEntity): RoundEntity {
        val game = gameFacade.load(gameId).let {
            gameModelFactory.create(it)
        }

        val round = game.dealNextRound(user)
        val activePlayers = game.getFourPlayersBehind(round.dealer)

        val hands = roundModelFactory.create(round).createPlayerHands(activePlayers)

        return roundRepository.save(round).also {
            handRepository.saveAll(hands.toList())
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