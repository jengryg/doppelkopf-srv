package game.doppelkopf.core.handler.round

import game.doppelkopf.core.cards.Card
import game.doppelkopf.core.cards.Deck
import game.doppelkopf.core.cards.DeckMode
import game.doppelkopf.core.common.enums.GameState
import game.doppelkopf.core.common.errors.GameFailedException
import game.doppelkopf.core.common.errors.ofForbiddenAction
import game.doppelkopf.core.common.errors.ofInvalidAction
import game.doppelkopf.core.model.game.GameModel
import game.doppelkopf.core.model.hand.HandModel
import game.doppelkopf.core.model.player.PlayerModel
import game.doppelkopf.core.model.round.RoundModel
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.persistence.model.hand.HandEntity
import game.doppelkopf.persistence.model.round.RoundEntity
import game.doppelkopf.utils.Quadruple
import org.springframework.lang.CheckReturnValue

class RoundDealHandler(
    val game: GameModel,
    val user: UserModel,
) {
    fun doHandle(): Pair<RoundEntity, Quadruple<HandEntity>> {
        canHandle().getOrThrow()

        val dealer = game.getCurrentDealerOrNull()
            ?: throw GameFailedException("Could not determine the dealer of the game.")

        val round = RoundEntity(
            game = game.entity,
            dealer = dealer.entity,
            number = game.rounds.size + 1
        ).let { RoundModel(it) }.also {
            game.addRound(it)
            game.state = GameState.PLAYING_ROUND
        }

        val activePlayers = game.getFourPlayersBehind(dealer)

        // Use the standard DeckMode for the initial card dealings.
        val handCards = Deck.create(DeckMode.DIAMONDS).dealHandCards()

        return Pair(
            first = round.entity,
            second = Quadruple(
                first = createAndAddHand(round, activePlayers.first, handCards.first).entity,
                second = createAndAddHand(round, activePlayers.second, handCards.second).entity,
                third = createAndAddHand(round, activePlayers.third, handCards.third).entity,
                fourth = createAndAddHand(round, activePlayers.fourth, handCards.fourth).entity,
            )
        )
    }

    @CheckReturnValue
    fun canHandle(): Result<Unit> {
        return when {
            game.state != GameState.WAITING_FOR_DEAL -> Result.ofInvalidAction(
                "Round:Create",
                "The game is currently not in the ${GameState.WAITING_FOR_DEAL.name} state."
            )

            game.getCurrentDealerOrNull()?.user?.id != user.id -> Result.ofForbiddenAction(
                "Round:Create",
                "Only the current dealer of the game can deal this round."
            )

            else -> Result.success(Unit)
        }
    }

    private fun createAndAddHand(round: RoundModel, player: PlayerModel, cards: List<Card>): HandModel {
        val encodedCards = cards.map { it.encoded }

        return HandEntity(
            round = round.entity,
            player = player.entity,
            cardsRemaining = encodedCards.toMutableList(),
            hasMarriage = cards.count { it.isQueenOfClubs } == 2
        ).let { HandModel(it) }.also {
            it.determineTeamByCards()
            round.addHand(it)
        }
    }
}