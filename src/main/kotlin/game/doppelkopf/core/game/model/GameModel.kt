package game.doppelkopf.core.game.model

import game.doppelkopf.core.errors.ForbiddenActionException
import game.doppelkopf.core.errors.InvalidActionException
import game.doppelkopf.core.play.model.RoundState
import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import game.doppelkopf.persistence.game.GameEntity
import game.doppelkopf.persistence.game.PlayerEntity
import game.doppelkopf.persistence.play.RoundEntity
import game.doppelkopf.persistence.user.UserEntity
import java.time.Instant

class GameModel(
    val game: GameEntity,
) : Logging {
    private val log = logger()

    fun start(user: UserEntity) {
        if (game.creator != user) {
            throw ForbiddenActionException("Game:Start", "Only the creator of the game can start it.")
        }

        if (game.state != GameState.INITIALIZED) {
            throw InvalidActionException("Game:Start", "The game has been started already.")
        }

        if (game.players.size < 4) {
            throw InvalidActionException("Game:Start", "The game needs to have at least 4 players.")
        }

        // the first dealer is decided randomly
        game.players.random().dealer = true

        // start the game
        game.started = Instant.now()
        game.state = GameState.WAITING_FOR_DEAL
    }

    fun join(user: UserEntity, seat: Int): PlayerEntity {
        if (game.state != GameState.INITIALIZED) {
            throw InvalidActionException("Game:Join", "You can not join a game that has already started.")
        }

        if (game.players.count() >= game.maxNumberOfPlayers) {
            throw InvalidActionException("Game:Join", "This game is already at its maximum capacity.")
        }

        if (game.players.any { it.user == user }) {
            throw InvalidActionException("Game:Join", "You already joined this game.")
        }

        if (game.players.any { it.seat == seat }) {
            throw InvalidActionException("Game:Join", "The seat you have chosen is already taken by another player.")
        }

        return PlayerEntity(
            user = user,
            game = game,
            seat = seat
        ).also {
            game.players.add(it)
        }
    }

    fun nextRound(user: UserEntity): RoundEntity {
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

        return round
    }
}