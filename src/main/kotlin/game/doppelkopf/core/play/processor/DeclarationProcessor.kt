package game.doppelkopf.core.play.processor

import game.doppelkopf.core.errors.GameFailedException
import game.doppelkopf.core.errors.ofInvalidAction
import game.doppelkopf.core.common.enums.Declaration
import game.doppelkopf.core.common.enums.RoundContract
import game.doppelkopf.core.common.enums.RoundState
import game.doppelkopf.persistence.play.RoundEntity

/**
 * [DeclarationProcessor] can only be instantiated if the round is ready.
 * See [DeclarationProcessor.canProcess] and [DeclarationProcessor.createWhenReady].
 */
class DeclarationProcessor private constructor(
    val round: RoundEntity
) {
    companion object {
        /**
         * @return success with [DeclarationProcessor] when the round is ready, failure otherwise
         */
        fun createWhenReady(round: RoundEntity): Result<DeclarationProcessor> {
            val guard = canProcess(round)

            guard.onFailure {
                return Result.failure(it)
            }

            return Result.success(DeclarationProcessor(round))
        }

        private fun canProcess(round: RoundEntity): Result<Unit> {
            return when {
                round.state != RoundState.INITIALIZED -> Result.ofInvalidAction(
                    "Declaration:Process",
                    "The round must be in INITIALIZED state to process the declarations."
                )

                round.hands.any { it.declared == Declaration.NOTHING } -> Result.ofInvalidAction(
                    "Declaration:Process",
                    "Not all players have finished their declaration yet."
                )

                else -> Result.success(Unit)
            }
        }
    }

    /**
     * Determine the result of the [Declaration] and proceed with [round] accordingly.
     */
    fun process() {
        val declarations = round.hands.groupBy { it.declared }.mapValues { it.value.size }
        val (reservation, healthy, silent) = listOf(
            Declaration.RESERVATION,
            Declaration.HEALTHY,
            Declaration.SILENT_MARRIAGE
        ).map {
            declarations[it] ?: 0
        }

        when {
            // Round declaration is completed, but since there is a reservation we need to continue with the auctioning.
            reservation > 0 -> advanceToBidding()

            // Round is a valid silent marriage, thus we can configure the round and skip auctioning.
            // It is necessary to mimic the behaviour of the normal round to hide the silent marriage.
            silent == 1 && healthy == 3 -> RoundConfigurator.configureSilentMarriageRound(round)

            // Round is a valid normal round, thus we can configure the round and skip auctioning.
            healthy == 4 -> RoundConfigurator.configureNormalRound(round)

            else -> throw GameFailedException("Can not determine declaration result.")
        }
    }

    /**
     * Configures this [round] to enter a bidding auction to determine the [RoundContract].
     */
    private fun advanceToBidding() {
        round.state = RoundState.DECLARED
    }
}