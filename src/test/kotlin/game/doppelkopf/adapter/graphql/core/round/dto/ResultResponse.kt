package game.doppelkopf.adapter.graphql.core.round.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.domain.hand.enums.Team
import java.util.*

data class ResultResponse(
    val id: UUID,
    val team: Team,
    val trickCount: Int,
    val scoreObtained: Int,
    val scoreTarget: Int,
    val points: PointsResponse,
    val cu: CreatedUpdated,
)