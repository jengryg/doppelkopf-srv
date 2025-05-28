package game.doppelkopf.adapter.graphql.core.round.dto

import game.doppelkopf.adapter.persistence.model.result.ResultEntity

data class PointsResult(
    val winning: Int,
    val teamCalls: PointsResultTeamCalls,
    val underCalls: PointsResultUnderCalls,
    val lostScore: ScoreQuadruple,
    val special: PointsResultSpecial,
) {
    constructor(entity: ResultEntity) : this(
        winning = entity.pointsForWinning,
        teamCalls = PointsResultTeamCalls(
            re = entity.pointsBasicCallsRe,
            ko = entity.pointsBasicCallsKo,
        ),
        underCalls = PointsResultUnderCalls(
            re = ScoreQuadruple(
                p90 = entity.pointsUnderCallsRe90,
                p60 = entity.pointsUnderCallsRe60,
                p30 = entity.pointsUnderCallsRe30,
                p00 = entity.pointsUnderCallsRe00,
            ),
            ko = ScoreQuadruple(
                p90 = entity.pointsUnderCallsKo90,
                p60 = entity.pointsUnderCallsKo60,
                p30 = entity.pointsUnderCallsKo30,
                p00 = entity.pointsUnderCallsKo00,
            )
        ),
        lostScore = ScoreQuadruple(
            p90 = entity.pointsLostScore90,
            p60 = entity.pointsLostScore60,
            p30 = entity.pointsLostScore30,
            p00 = entity.pointsLostScore00,
        ),
        special = PointsResultSpecial(
            opposition = entity.pointsForOpposition,
            doppelkopf = entity.pointsForDoppelkopf,
            charly = entity.pointsForCharly,
        )
    )
}
