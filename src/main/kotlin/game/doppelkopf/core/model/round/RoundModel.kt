package game.doppelkopf.core.model.round

import game.doppelkopf.core.cards.DeckMode
import game.doppelkopf.core.common.enums.*
import game.doppelkopf.core.common.errors.GameFailedException
import game.doppelkopf.core.common.errors.ofInvalidAction
import game.doppelkopf.core.model.IModelFactory
import game.doppelkopf.persistence.model.round.RoundEntity
import org.springframework.lang.CheckReturnValue
import java.util.*

class RoundModel private constructor(
    entity: RoundEntity
) : RoundModelAbstract(entity) {

    fun evaluateDeclarations() {
        canEvaluateDeclarations().getOrThrow()

        val declarations = hands.values.groupBy { it.declared }.mapValues { it.value.size }
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
                state = RoundState.WAITING_FOR_BIDS
            }

            // Round is a valid normal round, thus we can configure the round and skip auctioning.
            healthy == 4 -> {
                deckMode = DeckMode.DIAMONDS
                state = RoundState.PLAYING_TRICKS
                contract = RoundContract.NORMAL
            }

            // Round is a valid silent marriage, thus we can configure the round and skip auctioning.
            silent == 1 && healthy == 3 -> {
                deckMode = DeckMode.DIAMONDS
                state = RoundState.PLAYING_TRICKS
                contract = RoundContract.SILENT_MARRIAGE
            }

            else -> throw GameFailedException("Can not determine declaration result.")
        }
    }

    @CheckReturnValue
    fun canEvaluateDeclarations(): Result<Unit> {
        return when {
            state != RoundState.WAITING_FOR_DECLARATIONS -> Result.ofInvalidAction(
                "Declaration:Process",
                "The round must be in ${RoundState.WAITING_FOR_DECLARATIONS} state to process the declarations."
            )

            hands.values.any { it.declared == Declaration.NOTHING } -> Result.ofInvalidAction(
                "Declaration:Process",
                "Not all players have finished their declaration yet."
            )

            else -> Result.success(Unit)
        }
    }

    fun evaluateBidding() {
        canEvaluateBidding().getOrThrow()

        when {
            onlyMarriageBid() -> {
                deckMode = DeckMode.DIAMONDS
                state = RoundState.PLAYING_TRICKS
                contract = RoundContract.WEDDING

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

            // TODO: SOLO SYSTEM IMPLEMENTATION
            else -> throw GameFailedException("Can not determine auction result.")
        }
    }

    @CheckReturnValue
    fun canEvaluateBidding(): Result<Unit> {
        return when {
            state != RoundState.WAITING_FOR_BIDS -> Result.ofInvalidAction(
                "Bidding:Process",
                "The round must be in ${RoundState.WAITING_FOR_BIDS} state to process the bids."
            )

            hands.values.any {
                it.declared == Declaration.RESERVATION && it.bidding == Bidding.NOTHING
            } -> Result.ofInvalidAction(
                "Bidding:Process",
                "Not all players have finished their bids yet."
            )

            else -> Result.success(Unit)
        }
    }

    private fun onlyMarriageBid(): Boolean {
        return hands.values.all {
            (it.bidding == Bidding.NOTHING) || (it.bidding == Bidding.MARRIAGE)
        }
    }

    companion object : IModelFactory<RoundEntity, RoundModel> {
        private val instances = mutableMapOf<UUID, RoundModel>()

        override fun create(entity: RoundEntity): RoundModel {
            return instances.getOrPut(entity.id) { RoundModel(entity) }
        }
    }
}