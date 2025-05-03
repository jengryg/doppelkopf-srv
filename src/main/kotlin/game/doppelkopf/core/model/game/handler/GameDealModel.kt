package game.doppelkopf.core.model.game.handler

import game.doppelkopf.core.cards.Card
import game.doppelkopf.core.cards.Deck
import game.doppelkopf.core.cards.DeckMode
import game.doppelkopf.core.common.enums.GameState
import game.doppelkopf.core.common.enums.Team
import game.doppelkopf.core.errors.ofForbiddenAction
import game.doppelkopf.core.errors.ofInvalidAction
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.game.GameModelAbstract
import game.doppelkopf.core.model.hand.IHandModel
import game.doppelkopf.core.model.player.IPlayerModel
import game.doppelkopf.core.model.round.IRoundModel
import game.doppelkopf.core.model.user.IUserModel
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.utils.Quadruple
import org.springframework.lang.CheckReturnValue

class GameDealModel(
    entity: GameEntity,
    factoryProvider: ModelFactoryProvider
) : GameModelAbstract(entity, factoryProvider) {
    /**
     * Create a new round in this game dealing cards with [user] as the dealer.
     */
    fun deal(user: IUserModel): Pair<IRoundModel, Quadruple<IHandModel>> {
        val dealer = canDeal(user).getOrThrow()

        val round = factoryProvider.round.create(
            RoundEntity(
                game = entity,
                dealer = dealer.entity,
                number = rounds.size + 1
            )
        ).also {
            addRound(it)
        }

        val activePlayers =
            SeatingOrderResolver(entity = entity, factoryProvider = factoryProvider).getFourPlayersBehind(dealer)

        // Use the standard DeckMode for the initial card dealings.
        val handCards = Deck.create(DeckMode.DIAMONDS).dealHandCards()

        val hands = activePlayers.mapIndexed(handCards) { index, player, cards ->
            initializeEntity(round = round, player = player, index = index, cards = cards).let {
                factoryProvider.hand.create(it)
            }.also {
                round.addHand(it)
            }
        }

        // Advance the state of the game.
        state = GameState.PLAYING_ROUND

        return Pair(round, hands)
    }

    private fun initializeEntity(round: IRoundModel, player: IPlayerModel, index: Int, cards: List<Card>): HandEntity {
        return HandEntity(
            round = round.entity,
            player = player.entity,
            index = index,
            cardsRemaining = cards.map { it.encoded }.toMutableList(),
            hasMarriage = cards.count { it.isQueenOfClubs } == 2
        ).apply {
            internalTeam = when (cards.any { it.isQueenOfClubs }) {
                true -> Team.RE
                else -> Team.KO
            }
            playerTeam = internalTeam
        }
    }

    /**
     * Check if all conditions for [user] to deal a new round in the game are satisfied.
     */
    @CheckReturnValue
    fun canDeal(user: IUserModel): Result<IPlayerModel> {
        if (state != GameState.WAITING_FOR_DEAL) {
            return invalid("The game is currently not in the ${GameState.WAITING_FOR_DEAL.name} state.")
        }

        val dealer = getCurrentDealer().getOrElse {
            return invalid("The game does not have a designated dealer.")
        }

        return if (players[user] == dealer) {
            Result.success(dealer)
        } else {
            forbidden("Only the current dealer of the game can deal this round.")
        }
    }

    companion object {
        const val ACTION = "Game:Deal"

        fun forbidden(reason: String): Result<IPlayerModel> {
            return Result.ofForbiddenAction(ACTION, reason)
        }

        fun invalid(reason: String): Result<IPlayerModel> {
            return Result.ofInvalidAction(ACTION, reason)
        }
    }
}