package game.doppelkopf.core.handler.round

import game.doppelkopf.core.common.enums.Bidding
import game.doppelkopf.core.common.enums.Declaration
import game.doppelkopf.core.common.enums.RoundState
import game.doppelkopf.core.common.errors.GameFailedException
import game.doppelkopf.core.common.errors.ofInvalidAction
import game.doppelkopf.core.model.round.RoundModel
import game.doppelkopf.core.play.processor.RoundConfigurator
import game.doppelkopf.persistence.model.round.RoundEntity
import org.springframework.lang.CheckReturnValue

class RoundBiddingEvaluationHandler(
    val round: RoundModel
) {
    fun doHandle(): RoundEntity {
        canHandle().getOrThrow()

        when {
            // TODO: refactor round configurator
            onlyMarriageBid() -> RoundConfigurator.configureMarriageRound(round.entity)

            // TODO: SOLO SYSTEM IMPLEMENTATION
            else -> throw GameFailedException("Can not determine auction result.")
        }

        return round.entity
    }

    private fun onlyMarriageBid(): Boolean {
        return round.hands.all {
            (it.bidding == Bidding.NOTHING) || (it.bidding == Bidding.WEDDING)
        }
    }

    @CheckReturnValue
    fun canHandle(): Result<Unit> {
        return when {
            round.state != RoundState.DECLARED -> Result.ofInvalidAction(
                "Bidding:Process",
                "The round must be in declared state to process the bids."
            )

            round.hands.any {
                it.declared == Declaration.RESERVATION && it.bidding == Bidding.NOTHING
            } -> Result.ofInvalidAction(
                "Bidding:Process",
                "Not all players have finished their bids yet."
            )

            else -> Result.success(Unit)
        }
    }
}