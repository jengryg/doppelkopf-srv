package game.doppelkopf.domain.scores

import game.doppelkopf.domain.call.enums.CallType
import game.doppelkopf.domain.call.enums.CallTypeTargetReducing
import game.doppelkopf.domain.hand.enums.DefiniteTeam
import game.doppelkopf.domain.hand.enums.Team
import game.doppelkopf.domain.round.enums.DefiniteRoundWinner
import game.doppelkopf.utils.Teamed

/**
 * Evaluate the standard points based on the result of the round.
 *
 * @param scores the scores of each team
 * @param noTricks the number of tricks each team made
 * @param calls the list of [CallType] that each team made
 */
class GameResultTable(
    val scores: Teamed<Int>,
    val noTricks: Teamed<Boolean>,
    val calls: Teamed<List<CallType>>
) {
    /**
     * The [effectiveCalls] of each team is the most restrictive one, i.e. the highest [CallType.orderIndex] value.
     */
    val effectiveCalls = calls.map { list -> list.maxByOrNull { it.orderIndex } }

    /**
     * The targets of each team are influenced by their own calls and the other teams calls.
     */
    val targets: Teamed<Int> by lazy {
        val reducingTypes = effectiveCalls.map { it?.reducingType }

        when {
            // both teams made calls that reduce the necessary score of the opponent
            reducingTypes.re != null && reducingTypes.ko != null -> Teamed(
                // RE target is reduced by call of KO
                re = reducingTypes.ko.reduceTo,
                // KO target is reduced by call of RE
                ko = reducingTypes.re.reduceTo
            )

            // Only RE team made a reducing call.
            reducingTypes.re != null && reducingTypes.ko == null -> determineSingleSideReducedTargets(
                DefiniteTeam.RE,
                reducingTypes.re
            )

            // Only KO team made a reducing call.
            reducingTypes.re == null && reducingTypes.ko != null -> determineSingleSideReducedTargets(
                DefiniteTeam.KO,
                reducingTypes.ko
            )

            // No team made a reducing call.
            else -> determineSimpleTargets()
        }
    }

    private fun determineSimpleTargets(): Teamed<Int> {
        // When no target reducing calls (90, 60, 30, 0) were made, we only need to account for one special rule:
        return when {
            // Special case: When KO is called and RE has not called anything, RE only needs 120 and KO needs 121.
            effectiveCalls.re == null && effectiveCalls.ko == CallType.UNDER_120 -> Teamed(120, 121)
            // All other combinations of calls that do not reduce the targets: RE needs 121 and KO needs 120.
            else -> Teamed(121, 120)
        }
    }

    private fun determineSingleSideReducedTargets(
        callingTeam: DefiniteTeam,
        reduction: CallTypeTargetReducing
    ): Teamed<Int> {
        return Teamed(Unit, Unit).mapTagged { team, _ ->
            if (team == callingTeam) {
                reduction.increaseTo
            } else {
                reduction.reduceTo
            }
        }
    }

    /**
     * The [DefiniteRoundWinner] is the team that reaches its [targets] while the other team did not reach their target.
     */
    val winner: DefiniteRoundWinner by lazy {
        // Special check for 0 targets:
        // If the target is 0, the team can reach it by scoring just one trick, event this trick has 0 score value.
        val reachedZeroTarget = Teamed(
            re = targets.re == 0 && !noTricks.re,
            ko = targets.ko == 0 && !noTricks.ko
        )

        // Special check for 240 targets:
        // If the target is 240, the team can only reach it when the opposite team did not score any trick at all.
        val reachedFullTarget = Teamed(
            re = targets.re == 240 && noTricks.ko,
            ko = targets.ko == 240 && noTricks.re
        )

        // If 1 <= target <= 239, we just need to check if the score is equal or greater to that target.
        val reachedBYScore = scores.map(targets) { scr, tgt -> (tgt in (1..239)) && (scr >= tgt) }

        // The target was reached when one of the three criteria was met.
        reachedZeroTarget
            .map(reachedFullTarget) { x, y -> x || y }
            .map(reachedBYScore) { x, y -> x || y }
            .combine { re, ko ->
                when {
                    // RE reached target and KO not.
                    re && !ko -> DefiniteRoundWinner.RE
                    // KO reached target and RE not.
                    !re && ko -> DefiniteRoundWinner.KO
                    // If targets are lowered by both teams, it is possible that both reach their target.
                    // In that case, we have a DRAW.
                    else -> DefiniteRoundWinner.DRAW
                }
            }
    }

    /**
     * Basic points are granted to the [winner] of the game.
     * If there is no winner, these points are not granted (we assign [Team.NA] to the properties).
     */
    val basic: BasicPoints by lazy { BasicPoints(winner.team) }

    /**
     * Calls are granted to the [winner] of the game regardless of the team that made the call.
     * If there is no winner, these points are not granted (we assign [Team.NA] to the properties).
     */
    val basicCalls: BasicCalls
        get() {
            return BasicCalls(
                re = when {
                    calls.re.contains(CallType.UNDER_120) -> winner.team
                    else -> Team.NA
                },
                ko = when {
                    calls.ko.contains(CallType.UNDER_120) -> winner.team
                    else -> Team.NA
                }
            )
        }

    /**
     * Under calls of both teams are granted to the [winner] of the game regardless of the team that made the call.
     * If there is no winner, these points are not granted (we assign [Team.NA] to the properties).
     */
    val underCalls: Teamed<ScoreLevels> by lazy {
        calls.map { ct ->
            ScoreLevels(
                p90 = when {
                    ct.contains(CallType.UNDER_90) -> winner.team
                    else -> Team.NA
                },
                p60 = when {
                    ct.contains(CallType.UNDER_60) -> winner.team
                    else -> Team.NA
                },
                p30 = when {
                    ct.contains(CallType.UNDER_30) -> winner.team
                    else -> Team.NA
                },
                p00 = when {
                    ct.contains(CallType.NO_TRICKS) -> winner.team
                    else -> Team.NA
                },
            )
        }
    }

    /**
     * Lost score points are calculated based on the score of the losing team.
     */
    val lostScore: ScoreLevels by lazy {
        when {
            // RE is below 90 and is not the winner of the round.
            scores.re < 90 && winner != DefiniteRoundWinner.RE -> ScoreLevels(
                p90 = Team.KO,
                p60 = if (scores.re < 60) Team.KO else Team.NA,
                p30 = if (scores.re < 30) Team.KO else Team.NA,
                p00 = if (noTricks.re) Team.KO else Team.NA
            )

            // KO is below 90 and is not the winner of the round.
            scores.ko < 90 && winner != DefiniteRoundWinner.KO -> ScoreLevels(
                p90 = Team.RE,
                p60 = if (scores.ko < 60) Team.RE else Team.NA,
                p30 = if (scores.ko < 30) Team.RE else Team.NA,
                p00 = if (noTricks.ko) Team.RE else Team.NA
            )

            // Either both teams have at least 90 points, or the team that has the lowest score is the winner of the round.
            // This can be the case if a team wins with less than 90 points due to an under call made by the other team.
            // In this case, the lostScore points are not awarded.
            else -> ScoreLevels(Team.NA, Team.NA, Team.NA, Team.NA)
        }
    }

    /**
     * Beating calls of the RE team is assigned to the [Team.KO] if obtained, or [Team.NA] if not obtained.
     * Beating calls of the KO team is assigned to the [Team.RE] if obtained, or [Team.NA] if not obtained.
     */
    val beating: Teamed<ScoreLevels> by lazy {
        calls.swap() // calls of re are used for point calculation of ko and vice versa
            .mapTagged(scores) { team, cot, score ->
                ScoreLevels(
                    p90 = when {
                        // 90er call was made and beaten by 30 or more
                        cot.contains(CallType.UNDER_90) && score >= 120 -> team.internal
                        else -> Team.NA
                    },
                    p60 = when {
                        // 60er call was made and beaten by 30 or more
                        cot.contains(CallType.UNDER_60) && score >= 90 -> team.internal
                        else -> Team.NA
                    },
                    p30 = when {
                        // 30er call was made and beaten by 30 or more
                        cot.contains(CallType.UNDER_30) && score >= 60 -> team.internal
                        else -> Team.NA
                    },
                    p00 = when {
                        // 0er call was made and beaten by 30 or more
                        cot.contains(CallType.NO_TRICKS) && score >= 30 -> team.internal
                        else -> Team.NA
                    },
                )
            }
    }

    /**
     * Sums all the points awarded to each team.
     */
    val points: Teamed<Int> by lazy {
        val scoreList = listOf(
            underCalls.re,
            underCalls.ko,
            beating.re,
            beating.ko
        )

        Teamed(Unit, Unit).mapTagged { team, _ ->
            basic.getPointsFor(team) + basicCalls.getPointsFor(team) + lostScore.getPointsFor(team)
        }.mapTagged { team, pts ->
            pts + scoreList.sumOf { it.getPointsFor(team) }
        }
    }
}