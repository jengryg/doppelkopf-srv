package game.doppelkopf.core.model.trick.handler

import game.doppelkopf.core.common.enums.TrickState
import game.doppelkopf.core.errors.GameFailedException
import game.doppelkopf.core.errors.ofInvalidAction
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.trick.TrickModelAbstract
import game.doppelkopf.persistence.model.trick.TrickEntity
import org.slf4j.helpers.CheckReturnValue

class TrickEvaluationModel(
    entity: TrickEntity,
    factoryProvider: ModelFactoryProvider
) : TrickModelAbstract(
    entity,
    factoryProvider
) {
    fun evaluateTrick() {
        canEvaluateTrick().getOrThrow()

        // ensure that all the cached values are calculated from the finished trick
        updateCachedValues()

        // determine the index of the winning hand
        val winnerHandIndex = (openIndex + leadingCardIndex) % 4
        val hand = round.hands.values.singleOrNull { it.index == winnerHandIndex }
            ?: throw GameFailedException(
                "Can not determine the hand of winner with index $winnerHandIndex",
                entity.id
            )

        // Finalize the trick evaluation by setting the winners hand.
        setWinner(hand)
    }

    @CheckReturnValue
    fun canEvaluateTrick(): Result<Unit> {
        if (state != TrickState.FOURTH_CARD_PLAYED) {
            return invalid("The trick must be in ${TrickState.FOURTH_CARD_PLAYED} state to be evaluated.")
        }

        if (winner != null) {
            return invalid("The trick already has a winner determined.")
        }

        return Result.success(Unit)
    }

    companion object {
        const val ACTION = "Trick:Evaluate"

        fun <T> invalid(reason: String): Result<T> {
            return Result.ofInvalidAction(ACTION, reason)
        }
    }
}