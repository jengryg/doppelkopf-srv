package game.doppelkopf.core.handler.round

import game.doppelkopf.core.common.enums.Declaration
import game.doppelkopf.core.common.enums.RoundState
import game.doppelkopf.core.common.errors.GameFailedException
import game.doppelkopf.core.common.errors.ofInvalidAction
import game.doppelkopf.core.model.round.RoundModel
import game.doppelkopf.persistence.model.round.RoundEntity
import org.springframework.lang.CheckReturnValue

class RoundDeclarationEvaluationHandler(
    val round: RoundModel
) {
    fun doHandle(): RoundEntity {
        canHandle().getOrThrow()

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
            reservation > 0 -> {
                round.state = RoundState.WAITING_FOR_BIDS
            }

            // Round is a valid silent marriage, thus we can configure the round and skip auctioning.
            // It is necessary to mimic the behaviour of the normal round to hide the silent marriage.
            silent == 1 && healthy == 3 -> round.configureAsSilentMarriageRound()

            // Round is a valid normal round, thus we can configure the round and skip auctioning.
            healthy == 4 -> round.configureAsNormalRound()

            else -> throw GameFailedException("Can not determine declaration result.")
        }

        return round.entity
    }

    @CheckReturnValue
    fun canHandle(): Result<Unit> {
        return when {
            round.state != RoundState.WAITING_FOR_DECLARATIONS -> Result.ofInvalidAction(
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