package game.doppelkopf.core.model.round.handler

import game.doppelkopf.core.common.enums.RoundContract
import game.doppelkopf.core.common.enums.Team
import game.doppelkopf.core.errors.GameFailedException
import game.doppelkopf.core.errors.ofInvalidAction
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.hand.IHandModel
import game.doppelkopf.core.model.round.RoundModelAbstract
import game.doppelkopf.core.model.trick.ITrickModel
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import org.slf4j.helpers.CheckReturnValue

class RoundMarriageResolverModel(
    entity: RoundEntity,
    factoryProvider: ModelFactoryProvider
) : RoundModelAbstract(entity, factoryProvider) {

    fun resolveMarriage() {
        val (winner, trick) = canResolveMarriage().getOrThrow()

        val marriageHand = runCatching { hands.values.single { it.hasMarriage } }.getOrElse {
            throw GameFailedException("Could not determine the hand with the marriage.", entity.id, it)
        }

        if (winner.hasMarriage) {
            handleNotResolvedCase(trick, marriageHand)
        } else {
            handleResolvingCase(winner, marriageHand)
        }
    }

    @CheckReturnValue
    fun canResolveMarriage(): Result<Pair<IHandModel, ITrickModel>> {
        if (contract != RoundContract.MARRIAGE_UNRESOLVED) {
            return invalid("Only rounds that have the contract ${RoundContract.MARRIAGE_UNRESOLVED} can resolve a marriage.")
        }

        val trick = getCurrentTrick()
            ?: return invalid("Could not determine the last trick of the round $this.")

        val winner = trick.winner
            ?: return invalid("There is no winner in the current trick $trick to resolve the marriage with.")

        return Result.success(
            Pair(first = winner, second = trick)
        )
    }

    private fun handleNotResolvedCase(trick: ITrickModel, marriageHand: IHandModel) {
        if (trick.number <= 2) {
            // We allow the first 2 tricks to be non resolving without consequences.
            return
        } else {
            // If the marriage is still not resolved for trick number 3 or later, the hand with the marriage has to play solo.
            val others = hands.values.filter { it.id != marriageHand.id }.also {
                if (it.size != 3) {
                    throw GameFailedException("Could not determine the marriage solo opposition.", entity.id)
                }
            }

            // The hand that has the marriage must now play solo as RE team.
            marriageHand.assignPublicTeam(Team.RE)
            marriageHand.isMarried = false
            marriageHand.playsSolo = true

            // The other 3 hands are combined for the KO team.
            others.forEach {
                it.assignPublicTeam(Team.KO)
                it.isMarried = false
            }

            // The marriage of this round is resolved, but into a solo of the hand with the marriage.
            contract = RoundContract.MARRIAGE_SOLO
        }
    }

    private fun handleResolvingCase(winner: IHandModel, marriageHand: IHandModel) {
        // The winner marries the player with the marriage by joining them in the RE team.
        // The other two players are in the KO team.

        val others = hands.values.filter { it.id != winner.id && it.id != marriageHand.id }.also {
            if (it.size != 2) {
                throw GameFailedException("Could not determine the marriage opposition.", entity.id)
            }
        }

        // Combine the hand that has the marriage, and the given winner hand for the RE team.
        listOf(marriageHand, winner).forEach {
            it.assignPublicTeam(Team.RE)
            it.isMarried = true
        }

        // Combine the other two hands for the KO team.
        others.forEach {
            it.assignPublicTeam(Team.KO)
            it.isMarried = false
        }

        // The marriage of this round is now resolved.
        contract = RoundContract.MARRIAGE_RESOLVED
    }

    companion object {
        const val ACTION = "Marriage:Resolve"

        fun <T> invalid(reason: String): Result<T> {
            return Result.ofInvalidAction(ACTION, reason)
        }
    }
}