package game.doppelkopf.domain.round.service

import game.doppelkopf.adapter.persistence.model.result.ResultEntity
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.common.errors.GameFailedException
import game.doppelkopf.common.errors.ofInvalidAction
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.game.enums.GameState
import game.doppelkopf.domain.hand.enums.Team
import game.doppelkopf.domain.hand.model.IHandModel
import game.doppelkopf.domain.result.model.IResultModel
import game.doppelkopf.domain.round.enums.DefiniteRoundWinner
import game.doppelkopf.domain.round.enums.RoundState
import game.doppelkopf.domain.round.model.RoundModelAbstract
import game.doppelkopf.domain.scores.GameResultTable
import game.doppelkopf.domain.trick.model.ITrickModel
import game.doppelkopf.utils.Teamed
import org.springframework.lang.CheckReturnValue
import java.time.Instant

class RoundEvaluationModel(
    entity: RoundEntity,
    factoryProvider: ModelFactoryProvider
) : RoundModelAbstract(
    entity,
    factoryProvider
) {
    fun evaluateRound(): Teamed<IResultModel> {
        canEvaluateRound().getOrThrow()

        getTricksWonByHand().forEach { hand, tricks ->
            hand.score = tricks.sumOf { it.score }
            hand.tricks = tricks.count()
        }

        val wonTricksByTeam = getTricksWonByTeam()
        val wonTrickCount = wonTricksByTeam.map { list -> list.count() }

        val scoreByTeam = wonTricksByTeam.map { list -> list.sumOf { it.score } }
        val callsByTeam = getCalls().map { list -> list.map { it.callType } }

        val points = GameResultTable(
            scores = scoreByTeam,
            noTricks = wonTrickCount.map { it == 0 },
            calls = callsByTeam
        )

        // Doppelkopf points rule for each trick.
        val pointsForDoppelkopf = wonTricksByTeam.map { list -> list.count { it.isDoppelkopf() } }

        // Charly points rule for the last trick of the round.
        val pointsForCharly = calculatePointsForCharly(tricks.values.maxBy { it.number })

        // Opposition points rule when KO wins against RE.
        val pointsForOpposition = Teamed(
            re = 0,
            ko = if (points.winner == DefiniteRoundWinner.KO) 1 else 0
        )

        return scoreByTeam.mapTagged(wonTrickCount) { team, score, count ->
            ResultEntity(
                round = entity,
                team = team,
                score = score,
                trickCount = count,
                target = points.targets.get(team),

                pointsForWinning = team.oneIfMatches(points.basic.winning),

                pointsLostScore90 = team.oneIfMatches(points.lostScore.p90),
                pointsLostScore60 = team.oneIfMatches(points.lostScore.p60),
                pointsLostScore30 = team.oneIfMatches(points.lostScore.p30),
                pointsLostScore00 = team.oneIfMatches(points.lostScore.p00),

                pointsBasicCallsRe = team.oneIfMatches(points.basicCalls.re) * 2,
                pointsBasicCallsKo = team.oneIfMatches(points.basicCalls.ko) * 2,

                pointsUnderCallsRe90 = team.oneIfMatches(points.underCalls.re.p90),
                pointsUnderCallsKo90 = team.oneIfMatches(points.underCalls.ko.p90),
                pointsUnderCallsRe60 = team.oneIfMatches(points.underCalls.re.p60),
                pointsUnderCallsKo60 = team.oneIfMatches(points.underCalls.ko.p60),
                pointsUnderCallsRe30 = team.oneIfMatches(points.underCalls.re.p30),
                pointsUnderCallsKo30 = team.oneIfMatches(points.underCalls.ko.p30),
                pointsUnderCallsRe00 = team.oneIfMatches(points.underCalls.re.p00),
                pointsUnderCallsKo00 = team.oneIfMatches(points.underCalls.ko.p00),

                pointsBeatingRe90 = team.oneIfMatches(points.beating.re.p90),
                pointsBeatingKo90 = team.oneIfMatches(points.beating.ko.p90),
                pointsBeatingRe60 = team.oneIfMatches(points.beating.re.p60),
                pointsBeatingKo60 = team.oneIfMatches(points.beating.ko.p60),
                pointsBeatingRe30 = team.oneIfMatches(points.beating.re.p30),
                pointsBeatingKo30 = team.oneIfMatches(points.beating.ko.p30),
                pointsBeatingRe00 = team.oneIfMatches(points.beating.re.p00),
                pointsBeatingKo00 = team.oneIfMatches(points.beating.ko.p00),

                pointsForOpposition = pointsForOpposition.get(team),
                pointsForDoppelkopf = pointsForDoppelkopf.get(team),
                pointsForCharly = pointsForCharly.get(team),
            )
        }.map { factoryProvider.result.create(it) }.also { results ->
            addResult(results.re)
            addResult(results.ko)
            // add points to player and remove the dealer status
            hands.values.forEach { hand ->
                hand.player.dealer = false

                val factor = if (hand.playsSolo) 3 else 1

                val delta = when (hand.internalTeam) {
                    Team.RE -> results.re.getTotalPoints() - results.ko.getTotalPoints()
                    Team.KO -> results.ko.getTotalPoints() - results.re.getTotalPoints()
                    else -> 0
                }

                hand.player.points += factor * delta
            }
            // move dealer button to the player with hand index == 0, since hand index == 0 sits directly behind the
            // previous dealer in our seating order
            hands.values.single { it.index == 0 }.player.dealer = true

            // the round is done and the game is waiting for the deal (now from the new dealer)
            state = RoundState.EVALUATED
            ended = Instant.now()
            game.state = GameState.WAITING_FOR_DEAL
        }
    }


    private fun calculatePointsForCharly(lastTrick: ITrickModel): Teamed<Int> {
        if (!lastTrick.isCharly()) {
            return Teamed(0, 0)
        }

        return when (lastTrick.winner?.internalTeam) {
            Team.RE -> Teamed(1, 0)
            Team.KO -> Teamed(0, 1)
            else -> Teamed(0, 0)
        }
    }

    private fun getTricksWonByHand(): Map<IHandModel, List<ITrickModel>> {
        return tricks.values.groupBy {
            it.winner
                ?: throw GameFailedException("The trick $it does not have a winner yet.", entity.id)
        }
    }

    private fun getTricksWonByTeam(): Teamed<List<ITrickModel>> {
        return Teamed.filter(tricks.values) {
            it.winner?.internalTeam
                ?: throw GameFailedException("The trick $it does not have a winner yet.", entity.id)
        }
    }

    @CheckReturnValue
    fun canEvaluateRound(): Result<Unit> {
        if (state != RoundState.PLAYING_TRICKS) {
            return Result.ofInvalidAction("The round must be in ${RoundState.PLAYING_TRICKS} state to be evaluated.")
        }

        if (hands.values.any { it.size != 0 }) {
            return Result.ofInvalidAction("Not all hands of this round are empty.")
        }

        val trick = getCurrentTrick()
            ?: return Result.ofInvalidAction("Could not determine the last trick of the round $this.")

        if (trick.winner == null) {
            return Result.ofInvalidAction("There is no winner in the current $trick of this round.")
        }

        return Result.success(Unit)
    }
}