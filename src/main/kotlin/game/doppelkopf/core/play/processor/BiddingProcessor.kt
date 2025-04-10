package game.doppelkopf.core.play.processor

import game.doppelkopf.core.errors.GameFailedException
import game.doppelkopf.core.errors.ofInvalidAction
import game.doppelkopf.core.play.enums.Bidding
import game.doppelkopf.core.play.enums.Declaration
import game.doppelkopf.core.play.enums.RoundState
import game.doppelkopf.persistence.play.RoundEntity

/**
 * [BiddingProcessor] can only be instantiated if the round is ready.
 * See [BiddingProcessor.canProcess] and [BiddingProcessor.createWhenReady].
 */
class BiddingProcessor private constructor(
    val round: RoundEntity
) {
    /**
     * Determine the result of the [Bidding] and proceed with [round] accordingly.
     */
    fun process() {
        when {
            onlyMarriageBid() -> RoundConfigurator.configureMarriageRound(round)

            // TODO: SOLO SYSTEM IMPLEMENTATION
            else -> throw GameFailedException("Can not determine auction result.")
        }
    }

    private fun onlyMarriageBid(): Boolean {
        return round.hands.all {
            (it.bidding == Bidding.NOTHING) || (it.bidding == Bidding.WEDDING)
        }
    }

    companion object {
        /**
         * @return success with [BiddingProcessor] when the round is ready, failure otherwise
         */
        fun createWhenReady(round: RoundEntity): Result<BiddingProcessor> {
            val guard = canProcess(round)

            guard.onFailure {
                return Result.failure(it)
            }

            return Result.success(BiddingProcessor(round))
        }

        private fun canProcess(round: RoundEntity): Result<Unit> {
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
}