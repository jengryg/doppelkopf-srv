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
import game.doppelkopf.domain.trick.model.ITrickModel
import game.doppelkopf.utils.Quadruple
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
        val scoreByTeam = wonTricksByTeam.map { list -> list.sumOf { it.score } }
        val wonTrickCount = wonTricksByTeam.map { list -> list.count() }

        // TODO implement call system that modifies the score targets and allows additional points to be obtained.
        val targetScore = Teamed(re = 121, ko = 120)

        val roundWinner = determineWinner(
            targetScore = targetScore,
            reachedScore = scoreByTeam,
            trickCount = wonTrickCount,
        )

        val pointsForWinning = when (roundWinner) {
            DefiniteRoundWinner.RE -> Teamed(1, 0)
            DefiniteRoundWinner.KO -> Teamed(0, 1)
            DefiniteRoundWinner.DRAW -> Teamed(0, 0)
        }

        val pointsForOpposition = when (roundWinner) {
            DefiniteRoundWinner.RE -> Teamed(0, 0)
            DefiniteRoundWinner.KO -> Teamed(0, 1)
            DefiniteRoundWinner.DRAW -> Teamed(0, 0)
        }

        // Normal points rule for the below 90, below 60, below 30 and below 0 (no tricks at all).
        val pointsForScore = Teamed(
            // we need to invert re and ko here, since re scores based on the points of ko, and vice versa
            re = calculatePointsForScore(scoreByTeam.ko, wonTrickCount.ko),
            ko = calculatePointsForScore(scoreByTeam.re, wonTrickCount.re),
        )

        // Doppelkopf points rule for each trick.
        val pointsForDoppelkopf = wonTricksByTeam.map { list -> list.count { it.isDoppelkopf() } }

        // Charly points rule for the last trick of the round.
        val pointsForCharly = calculatePointsForCharly(tricks.values.maxBy { it.number })

        return scoreByTeam.mapTagged(wonTrickCount) { team, score, count ->
            ResultEntity(
                round = entity,
                team = team,
                score = score,
                trickCount = count,
                target = targetScore.get(team),
                pointsForWinning = pointsForWinning.get(team),
                pointsForOpposition = pointsForOpposition.get(team),
                pointsForScore090 = pointsForScore.get(team).first,
                pointsForScore060 = pointsForScore.get(team).second,
                pointsForScore030 = pointsForScore.get(team).third,
                pointsForScore000 = pointsForScore.get(team).fourth,
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
            // mode dealer button to the player with hand index == 0
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

    /**
     * One point for each of the following score marks (90,60,30,0):
     * - score < 90
     * - score < 60
     * - score < 30
     * - trickCount = 0
     *
     * @return a [Quadruple] with the marks in the order 90, 60, 30, 0
     */
    private fun calculatePointsForScore(score: Int, trickCount: Int): Quadruple<Int> {
        // The team loses 90, 60, 30 marks when their score is smaller than these marks.
        // The team only loses the 0 mark when their trickCount is 0.
        return Quadruple(90, 60, 30, 0).map {
            if (trickCount == 0 || it > score) 1 else 0
        }
    }

    private fun determineWinner(
        targetScore: Teamed<Int>,
        reachedScore: Teamed<Int>,
        trickCount: Teamed<Int>
    ): DefiniteRoundWinner {
        // Simple evaluation: when reached > target, the team satisfied the target
        // Special Case: when the target is 0, the team needs just one trick to satisfy the target, even if the trick
        // scored no points (e.g. 4 nines in one trick)
        val satisfied = Teamed(
            re = (reachedScore.re > targetScore.re) || (targetScore.re == 0 && trickCount.re > 0),
            ko = (reachedScore.ko > targetScore.ko) || (targetScore.ko == 0 && trickCount.ko > 0)
        )

        return when {
            // RE reached target and KO not
            satisfied.re && !satisfied.ko -> DefiniteRoundWinner.RE

            // KO reached target and RE not
            !satisfied.re && satisfied.ko -> DefiniteRoundWinner.KO

            // both reached their target (or both did not, math. impossible)
            else -> DefiniteRoundWinner.DRAW
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