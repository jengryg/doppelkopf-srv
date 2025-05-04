package game.doppelkopf.domain.round.service

import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.domain.deck.enums.DeckMode
import game.doppelkopf.domain.hand.enums.Declaration
import game.doppelkopf.domain.round.enums.RoundContract
import game.doppelkopf.domain.round.enums.RoundState
import game.doppelkopf.core.errors.GameFailedException
import game.doppelkopf.core.errors.ofForbiddenAction
import game.doppelkopf.core.errors.ofInvalidAction
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.round.model.RoundModelAbstract
import org.springframework.lang.CheckReturnValue

class RoundDeclarationEvaluationModel(
    entity: RoundEntity,
    factoryProvider: ModelFactoryProvider
) : RoundModelAbstract(entity, factoryProvider) {

    fun evaluateDeclarations() {
        val votes = canEvaluateDeclarations().getOrThrow()

        when {
            // Round declaration is completed, but since there is a reservation we need to continue with the auctioning.
            votes.reservation > 0 -> {
                state = RoundState.WAITING_FOR_BIDS
            }

            // Round is a valid normal round, thus we can configure the round and skip auctioning.
            votes.healthy == 4 -> {
                deckMode = DeckMode.DIAMONDS
                state = RoundState.PLAYING_TRICKS
                contract = RoundContract.NORMAL
            }

            // Round is a valid silent marriage, thus we can configure the round and skip auctioning.
            votes.silent == 1 && votes.healthy == 3 -> {
                deckMode = DeckMode.DIAMONDS
                state = RoundState.PLAYING_TRICKS
                contract = RoundContract.SILENT_MARRIAGE
            }

            else -> throw GameFailedException("Can not determine declaration result.", entity.game.id)
        }
    }

    @CheckReturnValue
    fun canEvaluateDeclarations(): Result<DeclarationResult> {
        if (state != RoundState.WAITING_FOR_DECLARATIONS) {
            return invalid(
                "The round must be in ${RoundState.WAITING_FOR_DECLARATIONS} state to process the declarations."
            )
        }

        val votes = calculateDeclarationResult()

        if (votes.nothing > 0) {
            return invalid(
                "Not all players have finished their declaration yet."
            )
        }

        return Result.success(votes)
    }

    inner class DeclarationResult(
        val nothing: Int,
        val reservation: Int,
        val healthy: Int,
        val silent: Int
    )

    private fun calculateDeclarationResult(): DeclarationResult {
        return hands.values.groupBy { it.declared }.mapValues { it.value.size }.let {
            DeclarationResult(
                nothing = it[Declaration.NOTHING] ?: 0,
                reservation = it[Declaration.RESERVATION] ?: 0,
                healthy = it[Declaration.HEALTHY] ?: 0,
                silent = it[Declaration.SILENT_MARRIAGE] ?: 0
            )
        }
    }

    companion object {
        const val ACTION = "Declarations:Evaluate"

        fun <T> forbidden(reason: String): Result<T> {
            return Result.ofForbiddenAction(ACTION, reason)
        }

        fun <T> invalid(reason: String): Result<T> {
            return Result.ofInvalidAction(ACTION, reason)
        }
    }
}