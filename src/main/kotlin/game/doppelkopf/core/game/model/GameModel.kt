package game.doppelkopf.core.game.model

import game.doppelkopf.core.errors.GameFailedException
import game.doppelkopf.core.errors.ofForbiddenAction
import game.doppelkopf.core.errors.ofInvalidAction
import game.doppelkopf.core.game.enums.GameState
import game.doppelkopf.core.play.enums.RoundState
import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import game.doppelkopf.persistence.game.GameEntity
import game.doppelkopf.persistence.game.PlayerEntity
import game.doppelkopf.persistence.play.RoundEntity
import game.doppelkopf.persistence.user.UserEntity
import game.doppelkopf.utils.Quadruple
import org.springframework.lang.CheckReturnValue
import java.time.Instant

class GameModel(
    val game: GameEntity,
) : Logging {
    private val log = logger()

    fun start(user: UserEntity): GameEntity {
        canStart(user).getOrThrow()

        // the first dealer is decided randomly
        game.players.random().dealer = true

        // start the game
        game.started = Instant.now()
        game.state = GameState.WAITING_FOR_DEAL

        return game
    }

    @CheckReturnValue
    fun canStart(user: UserEntity): Result<Unit> {
        return when {
            !userIsCreator(user) -> Result.ofForbiddenAction(
                "Game:Start",
                "Only the creator of the game can start it."
            )

            !isInInitializeState() -> Result.ofInvalidAction(
                "Game:Start",
                "The game has been started already."
            )

            !hasAtLeastFourPlayers() -> Result.ofInvalidAction(
                "Game:Start",
                "The game needs to have at least 4 players."
            )

            else -> Result.success(Unit)
        }
    }

    fun join(user: UserEntity, seat: Int): PlayerEntity {
        canJoin(user, seat).getOrThrow()

        return PlayerEntity(
            user = user,
            game = game,
            seat = seat
        ).also {
            game.players.add(it)
        }
    }

    @CheckReturnValue
    fun canJoin(user: UserEntity, seat: Int): Result<Unit> {
        return when {
            !isInInitializeState() -> Result.ofInvalidAction(
                "Game:Join",
                "You can not join a game that has already started."
            )

            !hasPlayerCapacity() -> Result.ofInvalidAction(
                "Game:Join",
                "This game is already at its maximum capacity."
            )

            !userIsNotPlayer(user) -> Result.ofInvalidAction(
                "Game:Join",
                "You already joined this game."
            )

            !isSeatFree(seat) -> Result.ofInvalidAction(
                "Game:Join",
                "The seat you have chosen is already taken by another player."
            )

            else -> Result.success(Unit)
        }
    }

    fun dealNextRound(user: UserEntity): RoundEntity {
        val dealer = getValidDealer(user).getOrThrow()
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

        return round
    }


    @CheckReturnValue
    fun getValidDealer(user: UserEntity): Result<PlayerEntity> {
        if (!isWaitingForDeal()) {
            return Result.ofInvalidAction(
                "Round:Create",
                "The game is currently not in the ${GameState.WAITING_FOR_DEAL.name} state."
            )
        }

        val dealer = game.getPlayerOfOrNull(user)?.takeIf { it.dealer }

        return when (dealer) {
            null -> Result.ofForbiddenAction(
                "Round:Create",
                "Only the current dealer of the game can start this round."
            )

            else -> Result.success(dealer)
        }
    }

    private fun userIsCreator(user: UserEntity): Boolean {
        return game.creator == user
    }

    private fun isInInitializeState(): Boolean {
        return game.state == GameState.INITIALIZED
    }

    private fun hasAtLeastFourPlayers(): Boolean {
        return game.players.size >= 4
    }

    private fun hasPlayerCapacity(): Boolean {
        return game.players.size < game.maxNumberOfPlayers
    }

    private fun userIsNotPlayer(user: UserEntity): Boolean {
        return game.players.all { it.user != user }
    }

    private fun isSeatFree(seat: Int): Boolean {
        return game.players.all { it.seat != seat }
    }

    private fun isWaitingForDeal(): Boolean {
        return game.state == GameState.WAITING_FOR_DEAL
    }

    /**
     * Determines the 4 players in order that sit behind the given [player].
     * Order is calculated circular in ascending order of seat numbers starting from the seat number of the [player].
     *
     * Note: If a game has only 4 players, the last active player is the returned [Quadruple] is [player].
     *
     * @return the [Quadruple] of the players behind [player]
     */
    fun getFourPlayersBehind(player: PlayerEntity): Quadruple<PlayerEntity> {
        if (!hasAtLeastFourPlayers()) {
            throw GameFailedException("Can not determine 4 players when game has only ${game.players.size} players.")
        }

        val sortedPlayers = game.players.sortedBy { it.seat }
        val startIndex = sortedPlayers.indexOf(player).takeIf { it != -1 }
            ?: throw GameFailedException("Could not determine the position of $player.")

        // 0 is the given player, we start with the player sitting directly behind, i.e. with position 1
        val players = (1..4).map { position ->
            sortedPlayers[(startIndex + position) % sortedPlayers.size]
        }

        return Quadruple(players[0], players[1], players[2], players[3])
    }
}