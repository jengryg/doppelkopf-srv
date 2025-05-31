package game.doppelkopf.adapter.graphql.core.round.dto

import game.doppelkopf.utils.Teamed

data class PointsResponse(
    val winning: Int,
    val teamCalls: Teamed<Int>,
    val underCalls: Teamed<ScoreQuadrupleResponse>,
    val lostScore: ScoreQuadrupleResponse,
    val special: PointsSpecialResponse,
)
