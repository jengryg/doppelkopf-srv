package game.doppelkopf.domain.round.handler

import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.domain.cards.DeckMode
import game.doppelkopf.core.common.enums.*
import game.doppelkopf.core.errors.GameFailedException
import game.doppelkopf.core.errors.ofForbiddenAction
import game.doppelkopf.core.errors.ofInvalidAction
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.domain.round.model.RoundModelAbstract
import org.springframework.lang.CheckReturnValue

class RoundBidsEvaluationModel(
    entity: RoundEntity,
    factoryProvider: ModelFactoryProvider
) : RoundModelAbstract(entity, factoryProvider) {

    fun evaluateBids() {
        val votes = canEvaluateBids().getOrThrow()

        when {
            votes.marriage == 1 && votes.solo == 0 -> configureMarriageRound()
            // TODO: SOLO SYSTEM IMPLEMENTATION
            else -> throw GameFailedException("Can not determine bid result.", entity.game.id)
        }
    }

    @CheckReturnValue
    fun canEvaluateBids(): Result<BidResult> {
        if (state != RoundState.WAITING_FOR_BIDS) {
            return invalid(
                "The round must be in ${RoundState.WAITING_FOR_BIDS} state to process the bids."
            )
        }

        val votes = calculateBidResult()

        if (votes.nothing > 0) {
            return invalid(
                "Not all players have finished their bids yet."
            )
        }

        return Result.success(votes)
    }

    inner class BidResult(
        val nothing: Int,
        val marriage: Int,
        val solo: Int
    )

    private fun calculateBidResult(): BidResult {
        return BidResult(
            nothing = hands.values.count { it.bidding == Bidding.NOTHING && it.declared == Declaration.RESERVATION },
            marriage = hands.values.count { it.bidding == Bidding.MARRIAGE },
            solo = hands.values.count { it.bidding.isSolo }
        )
    }

    private fun configureMarriageRound() {
        deckMode = DeckMode.DIAMONDS
        state = RoundState.PLAYING_TRICKS
        contract = RoundContract.MARRIAGE_UNRESOLVED

        hands.values.forEach {
            if (it.hasMarriage) {
                // Team of player with marriage on hand is already public.
                it.internalTeam = Team.RE
                it.playerTeam = Team.RE
                it.publicTeam = Team.RE
            } else {
                // We consider the other players to be in no team until the marriage is resolved during the play.
                it.internalTeam = Team.NA
                it.playerTeam = Team.NA
                it.publicTeam = Team.NA
            }
        }
    }

    companion object {
        const val ACTION = "Bids:Evaluate"

        fun <T> forbidden(reason: String): Result<T> {
            return Result.ofForbiddenAction(ACTION, reason)
        }

        fun <T> invalid(reason: String): Result<T> {
            return Result.ofInvalidAction(ACTION, reason)
        }
    }
}