package game.doppelkopf.domain.round.service

import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.common.errors.GameFailedException
import game.doppelkopf.common.errors.ofInvalidAction
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.hand.enums.Bidding
import game.doppelkopf.domain.hand.enums.Declaration
import game.doppelkopf.domain.hand.enums.Team
import game.doppelkopf.domain.hand.model.IHandModel
import game.doppelkopf.domain.round.enums.RoundState
import game.doppelkopf.domain.round.model.RoundModelAbstract
import org.springframework.lang.CheckReturnValue

class RoundBidsEvaluationModel(
    entity: RoundEntity,
    factoryProvider: ModelFactoryProvider
) : RoundModelAbstract(entity, factoryProvider) {

    fun evaluateBids() {
        val votes = canEvaluateBids().getOrThrow()

        val solo = votes.solo.minByOrNull { it.index }

        if (solo != null) {
            // if there is a solo bidding, this is a solo round
            configureSoloRound(solo)
        } else {
            // no solo, but we are in a bidding phase, so there must be a marriage bid then
            val marriage = votes.marriage.singleOrNull()
                ?: throw GameFailedException("Invalid bid result detected.", entity.game.id)

            configureMarriageRound(marriage)
        }
    }

    @CheckReturnValue
    fun canEvaluateBids(): Result<BidResult> {
        if (state != RoundState.WAITING_FOR_BIDS) {
            return Result.ofInvalidAction(
                "The round must be in ${RoundState.WAITING_FOR_BIDS} state to process the bids."
            )
        }

        val votes = calculateBidResult()

        if (votes.nothing.isNotEmpty()) {
            return Result.ofInvalidAction(
                "Not all players have finished their bids yet."
            )
        }

        return Result.success(votes)
    }

    class BidResult(
        val nothing: List<IHandModel>,
        val marriage: List<IHandModel>,
        val solo: List<IHandModel>,
    )

    private fun calculateBidResult(): BidResult {
        return BidResult(
            nothing = hands.values.filter { it.bidding == Bidding.NOTHING && it.declared == Declaration.RESERVATION },
            marriage = hands.values.filter { it.bidding == Bidding.MARRIAGE },
            solo = hands.values.filter { it.bidding.isSolo }
        )
    }

    private fun configureMarriageRound(marriageHand: IHandModel) {
        deckMode = marriageHand.bidding.deckMode
        contract = marriageHand.bidding.roundContract
        state = RoundState.PLAYING_TRICKS

        hands.values.forEach {
            if (it == marriageHand) {
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

    private fun configureSoloRound(soloHand: IHandModel) {
        deckMode = soloHand.bidding.deckMode
        contract = soloHand.bidding.roundContract
        state = RoundState.PLAYING_TRICKS

        hands.values.forEach {
            if (it == soloHand) {
                it.playsSolo = true
                // Team of the solo player is already public.
                it.internalTeam = Team.RE
                it.playerTeam = Team.RE
                it.publicTeam = Team.RE
            } else {
                // Team of the other players is also public.
                it.internalTeam = Team.KO
                it.playerTeam = Team.KO
                it.publicTeam = Team.KO
            }
        }
    }
}