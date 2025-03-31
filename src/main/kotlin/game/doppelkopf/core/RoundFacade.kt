package game.doppelkopf.core

import game.doppelkopf.core.game.model.GameModelFactory
import game.doppelkopf.persistence.EntityNotFoundException
import game.doppelkopf.persistence.play.RoundEntity
import game.doppelkopf.persistence.play.RoundRepository
import game.doppelkopf.persistence.user.UserEntity
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class RoundFacade(
    private val gameFacade: GameFacade,
    private val roundRepository: RoundRepository,
    private val gameModelFactory: GameModelFactory,
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

        val round = game.nextRound(user)

        // TODO: deal the actual cards to the players

        return roundRepository.save(round)
    }
}