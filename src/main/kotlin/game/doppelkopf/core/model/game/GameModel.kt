package game.doppelkopf.core.model.game

import game.doppelkopf.core.cards.Deck
import game.doppelkopf.core.cards.DeckMode
import game.doppelkopf.core.common.enums.GameState
import game.doppelkopf.core.common.enums.Team
import game.doppelkopf.core.common.errors.ofForbiddenAction
import game.doppelkopf.core.common.errors.ofInvalidAction
import game.doppelkopf.core.model.IModelFactory
import game.doppelkopf.core.model.hand.HandModel
import game.doppelkopf.core.model.player.PlayerModel
import game.doppelkopf.core.model.round.RoundModel
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.persistence.model.game.GameEntity
import game.doppelkopf.persistence.model.hand.HandEntity
import game.doppelkopf.persistence.model.player.PlayerEntity
import game.doppelkopf.persistence.model.round.RoundEntity
import game.doppelkopf.utils.Quadruple
import org.springframework.lang.CheckReturnValue
import java.time.Instant
import java.util.*

class GameModel private constructor(
    entity: GameEntity
) : GameModelAbstract(entity) {
    /**
     * Start the game by marking the [started] time and transition the [state] of the game to
     * [GameState.WAITING_FOR_DEAL]. Choose one of the [players] randomly as first dealer of the game.
     */
    fun start(user: UserModel) {
        canStart(user).getOrThrow()
        // the first dealer is decided randomly
        players.values.forEach { it.dealer = false }
        // TODO: seeded randomness for seed based games
        players.values.random().dealer = true

        started = Instant.now()
        state = GameState.WAITING_FOR_DEAL
    }

    /**
     * Check if all conditions for [user] to start the game are satisfied.
     */
    @CheckReturnValue
    fun canStart(user: UserModel): Result<Unit> {
        return when {
            creator != user -> Result.ofForbiddenAction(
                "Game:Start",
                "Only the creator of the game can start it."
            )

            state != GameState.INITIALIZED -> Result.ofInvalidAction(
                "Game:Start",
                "The game has been started already."
            )

            players.size < 4 -> Result.ofInvalidAction(
                "Game:Start",
                "The game needs to have at least 4 players."
            )

            else -> Result.success(Unit)
        }
    }

    /**
     * Join the game as [user] on [seat] by creating a new player and adding it to this game.
     */
    fun join(user: UserModel, seat: Int): PlayerModel {
        canJoin(user, seat).getOrThrow()

        return PlayerModel.create(
            PlayerEntity(user = user.entity, game = entity, seat = seat)
        ).also {
            addPlayer(it)
        }
    }

    /**
     * Check if all conditions for [user] to join the game on [seat] are satisfied.
     */
    @CheckReturnValue
    fun canJoin(user: UserModel, seat: Int): Result<Unit> {
        return when {
            state != GameState.INITIALIZED -> Result.ofInvalidAction(
                "Game:Join",
                "You can not join a game that has already started."
            )

            players.size >= maxNumberOfPlayers -> Result.ofInvalidAction(
                "Game:Join",
                "This game is already at its maximum capacity."
            )

            players[user] != null -> Result.ofInvalidAction(
                "Game:Join",
                "You already joined this game."
            )

            players.values.any { it.seat == seat } -> Result.ofInvalidAction(
                "Game:Join",
                "The seat you have chosen is already taken by another player."
            )

            else -> Result.success(Unit)
        }
    }

    fun deal(user: UserModel): Pair<RoundModel, Quadruple<HandModel>> {
        val dealer = canDeal(user).getOrThrow()

        val round = RoundModel.create(
            RoundEntity(
                game = entity,
                dealer = dealer.entity,
                number = rounds.size + 1
            )
        )

        addRound(round)

        val activePlayers = getFourPlayersBehind(dealer)

        // Use the standard DeckMode for the initial card dealings.
        val handCards = Deck.create(DeckMode.DIAMONDS).dealHandCards()

        val hands = activePlayers.map(handCards) { player, cards ->
            HandModel.create(
                HandEntity(
                    round = round.entity,
                    player = player.entity,
                    cardsRemaining = cards.map { it.encoded }.toMutableList(),
                    hasMarriage = cards.count { it.isQueenOfClubs } == 2
                ).apply {
                    internalTeam = when (cards.any { it.isQueenOfClubs }) {
                        true -> Team.RE
                        else -> Team.KO
                    }
                    playerTeam = internalTeam
                }
            ).also {
                round.addHand(it)
            }
        }

        state = GameState.PLAYING_ROUND

        return Pair(
            first = round,
            second = hands
        )
    }

    @CheckReturnValue
    fun canDeal(user: UserModel): Result<PlayerModel> {
        return when {
            state != GameState.WAITING_FOR_DEAL -> Result.ofInvalidAction(
                "Round:Create",
                "The game is currently not in the ${GameState.WAITING_FOR_DEAL.name} state."
            )

            else -> players[user]?.takeIf { it.dealer }?.let { Result.success(it) } ?: Result.ofForbiddenAction(
                "Round:Create",
                "Only the current dealer of the game can deal this round."
            )
        }
    }

    companion object : IModelFactory<GameEntity, GameModel> {
        private val instances = mutableMapOf<UUID, GameModel>()

        override fun create(entity: GameEntity): GameModel {
            return instances.getOrPut(entity.id) { GameModel(entity) }
        }
    }
}