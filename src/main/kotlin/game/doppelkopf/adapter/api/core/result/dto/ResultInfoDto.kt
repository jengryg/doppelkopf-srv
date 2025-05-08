package game.doppelkopf.adapter.api.core.result.dto

import game.doppelkopf.adapter.persistence.model.result.ResultEntity
import game.doppelkopf.domain.hand.enums.DefiniteTeam
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(
    description = "Information about a result of a round in a game of Doppelkopf."
)
class ResultInfoDto(
    @Schema(
        description = "The UUID of this result."
    )
    val id: UUID,

    @Schema(
        description = "The UUID of the round this result is from."
    )
    val roundId: UUID,

    @Schema(
        description = "The team this result is for."
    )
    val team: DefiniteTeam,

    @Schema(
        description = "The number of tricks this team obtained."
    )
    val tricksCount: Int,

    @Schema(
        description = "The total score this team obtained."
    )
    val score: Int,

    @Schema(
        description = "The target score this team must obtain."
    )
    val targetScore: Int,

    @Schema(
        description = "The points this team obtained for winning."
    )
    val pointsForWinning: Int,

    @Schema(
        description = "The extra point when KO wins against RE."
    )
    val pointsForOpposition: Int,

    @Schema(
        description = "The extra point when the loosing team scores below 90."
    )
    val pointsForScore090: Int,

    @Schema(
        description = "The extra point when the loosing team scores below 60."
    )
    val pointsForScore060: Int,

    @Schema(
        description = "The extra point when the loosing team scores below 30."
    )
    val pointsForScore030: Int,

    @Schema(
        description = "The extra point when the loosing team scores no trick at all."
    )
    val pointsForScore000: Int,

    @Schema(
        description = "The points this team obtained for winning tricks with a score of 40 or more."
    )
    val pointsForDoppelkopf: Int,

    @Schema(
        description = "The points this team obtained for winning the last trick with Jack of Clubs."
    )
    val pointsForCharly: Int,
) {
    constructor(resultEntity: ResultEntity) : this(
        id = resultEntity.id,
        roundId = resultEntity.round.id,
        team = resultEntity.team,
        tricksCount = resultEntity.trickCount,
        score = resultEntity.score,
        targetScore = resultEntity.score,
        pointsForWinning = resultEntity.pointsForWinning,
        pointsForOpposition = resultEntity.pointsForOpposition,
        pointsForScore090 = resultEntity.pointsForScore090,
        pointsForScore060 = resultEntity.pointsForScore060,
        pointsForScore030 = resultEntity.pointsForScore030,
        pointsForScore000 = resultEntity.pointsForScore000,
        pointsForDoppelkopf = resultEntity.pointsForDoppelkopf,
        pointsForCharly = resultEntity.pointsForCharly,
    )
}