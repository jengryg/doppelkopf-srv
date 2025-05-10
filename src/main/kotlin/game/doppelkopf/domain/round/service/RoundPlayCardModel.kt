package game.doppelkopf.domain.round.service

import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.trick.TrickEntity
import game.doppelkopf.adapter.persistence.model.turn.TurnEntity
import game.doppelkopf.common.errors.GameFailedException
import game.doppelkopf.common.errors.ofForbiddenAction
import game.doppelkopf.common.errors.ofInvalidAction
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.deck.model.Card
import game.doppelkopf.domain.hand.model.IHandModel
import game.doppelkopf.domain.hand.service.HandCardPlayModel
import game.doppelkopf.domain.round.enums.RoundState
import game.doppelkopf.domain.round.model.RoundModelAbstract
import game.doppelkopf.domain.trick.model.ITrickModel
import game.doppelkopf.domain.trick.service.TrickCardPlayModel
import game.doppelkopf.domain.turn.model.ITurnModel
import game.doppelkopf.domain.user.model.IUserModel
import org.springframework.lang.CheckReturnValue

class RoundPlayCardModel(
    entity: RoundEntity,
    factoryProvider: ModelFactoryProvider
) : RoundModelAbstract(entity, factoryProvider) {

    fun playCard(card: Card, user: IUserModel): Pair<ITrickModel, ITurnModel> {
        val (hand, currentTrick) = canPlayCard(user).getOrThrow()

        val trick = if (currentTrick == null) {
            // There is no current trick to serve, this card is the first card of a new trick.
            // The card determines the demand of the trick we create.
            hand.playCard(card, card.demand).getOrThrow()
            createNewTrick(hand.index, card)
        } else {
            // There is already a running trick that needs to be served.
            // The hand must serve the trick demand if it can.
            hand.playCard(card, currentTrick.demand).getOrThrow()
            currentTrick.also {
                it.playCard(card)
            }
        }

        val turn = TurnEntity(
            round = entity,
            hand = hand.entity,
            trick = trick.entity,
            number = entity.turns.size + 1,
            card = card.encoded
        ).let { factoryProvider.turn.create(it) }

        return Pair(trick, turn)
    }

    private fun createNewTrick(openIndex: Int, firstCard: Card): TrickCardPlayModel {
        return TrickEntity(
            round = entity,
            openIndex = openIndex,
            number = entity.tricks.size + 1,
            demand = firstCard.demand
        ).let { TrickCardPlayModel(entity = it, factoryProvider = factoryProvider) }.also {
            addTrick(it)
            it.playCard(firstCard)
        }
    }

    @CheckReturnValue
    fun canPlayCard(user: IUserModel): Result<Pair<HandCardPlayModel, TrickCardPlayModel?>> {
        if (state != RoundState.PLAYING_TRICKS) {
            return Result.ofInvalidAction("The round must be in ${RoundState.PLAYING_TRICKS} state to open a new trick.")
        }

        val hand = hands[user] ?: return Result.ofForbiddenAction("You are not playing in this round.")

        if (entity.tricks.isEmpty()) {
            // There is no trick in this round yet.
            // The hand of the user must be the one with index 0, since this is the one directly behind the dealer.
            return canPlayFirstTrickOfRound(hand)
        }

        // We already established that this round must have at least one trick.
        val trick = getCurrentTrick() ?: throw GameFailedException(
            "Could not determine the current trick of the current round $this.",
            entity.id
        )

        val winner = trick.winner

        if (winner != null) {
            // The trick already has a winner, thus the winner must start the next trick.
            // We indicate the need to add a new trick to the round by returning null for the trick here:
            return canOpenNextTrickOfRound(winner, hand)
        }

        // The current trick is not finished yet, thus the player must contribute a card into it.
        return canPlayIntoExistingTrick(trick, hand)
    }

    private fun canPlayFirstTrickOfRound(hand: IHandModel): Result<Pair<HandCardPlayModel, TrickCardPlayModel?>> {
        return if (hand.index != 0) {
            Result.ofForbiddenAction("Only the player directly behind the dealer can open the first trick of the round.")
        } else {
            Result.success(
                Pair(
                    HandCardPlayModel(hand.entity, factoryProvider),
                    null
                )
            )
        }
    }

    private fun canOpenNextTrickOfRound(
        previousTrickWinner: IHandModel,
        hand: IHandModel
    ): Result<Pair<HandCardPlayModel, TrickCardPlayModel?>> {
        return if (previousTrickWinner == hand) {
            Result.success(
                Pair(
                    HandCardPlayModel(hand.entity, factoryProvider),
                    null
                )
            )
        } else {
            Result.ofForbiddenAction("Only the winner of the previous trick can open the next trick of the round.")
        }
    }

    private fun canPlayIntoExistingTrick(
        trick: ITrickModel,
        hand: IHandModel
    ): Result<Pair<HandCardPlayModel, TrickCardPlayModel?>> {
        return if (trick.getExpectedHandIndex() == hand.index) {
            Result.success(
                Pair(
                    HandCardPlayModel(hand.entity, factoryProvider),
                    TrickCardPlayModel(trick.entity, factoryProvider)
                )
            )
        } else {
            Result.ofForbiddenAction("It is not your turn to play a card. ${hand.index} vs ${trick.getExpectedHandIndex()}")
        }
    }
}