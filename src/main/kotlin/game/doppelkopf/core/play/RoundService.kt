package game.doppelkopf.core.play

import game.doppelkopf.core.errors.ForbiddenActionException
import game.doppelkopf.core.errors.InvalidActionException
import game.doppelkopf.core.game.GameState
import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import game.doppelkopf.persistence.EntityNotFoundException
import game.doppelkopf.persistence.game.GameEntity
import game.doppelkopf.persistence.game.GameRepository
import game.doppelkopf.persistence.play.RoundEntity
import game.doppelkopf.persistence.play.RoundRepository
import game.doppelkopf.persistence.user.UserEntity
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class RoundService(
    private val gameRepository: GameRepository,
    private val roundRepository: RoundRepository,
) : Logging {
    private val log = logger()

    fun list(gameId: UUID): List<RoundEntity> {
        return gameRepository.findByIdOrNull(gameId)?.rounds?.toList()
            ?: throw EntityNotFoundException.forEntity<GameEntity>(gameId)
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
        val game = gameRepository.findByIdOrNull(gameId)
            ?: throw EntityNotFoundException.forEntity<GameEntity>(gameId)

        if (game.state != GameState.WAITING_FOR_DEAL) {
            throw InvalidActionException(
                "Round:Create",
                "The game is currently not in the ${GameState.WAITING_FOR_DEAL.name} state."
            )
        }

        val dealer = game.getPlayerOfOrNull(user)?.takeIf { it.dealer } ?: throw ForbiddenActionException(
            "Round:Create",
            "Only the current dealer of the game can start this round."
        )

        val previousRound = game.getLatestRoundOrNull()

        val round = RoundEntity(
            game = game,
            dealer = dealer,
            number = (previousRound?.number ?: 0) + 1
        ).apply {
            started = Instant.now()
            state = RoundState.INITIALIZED
        }

        game.rounds.add(round)
        game.state = GameState.PLAYING_ROUND

        log.atDebug()
            .setMessage("Created new round.")
            .addKeyValue("round") { round.toString() }
            .addKeyValue("dealer") { dealer.toString() }
            .addKeyValue("number") { round.number }
            .log()

        // TODO: deal the actual cards to the players

        return roundRepository.save(round)
    }
}