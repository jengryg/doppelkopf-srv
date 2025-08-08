package game.doppelkopf.adapter.api.core.result.dto

import game.doppelkopf.adapter.persistence.model.result.ResultEntity
import game.doppelkopf.domain.hand.enums.DefiniteTeam
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(
    description = "Information about a result of a round in a game of Doppelkopf."
)
class ResultInfoResponse(
    @field:Schema(
        description = "The UUID of this result."
    )
    val id: UUID,

    @field:Schema(
        description = "The UUID of the round this result is from."
    )
    val roundId: UUID,

    @field:Schema(
        description = "The team this result is for."
    )
    val team: DefiniteTeam,

    @field:Schema(
        description = "The number of tricks this team obtained."
    )
    val tricksCount: Int,

    @field:Schema(
        description = "The total score this team obtained."
    )
    val score: Int,

    @field:Schema(
        description = "The target score this team must obtain."
    )
    val targetScore: Int,

    @field:Schema(
        description = "The basic points for this team."
    )
    val pointsBasic: BasicPointsResponse,

    @field:Schema(
        description = "The points for the basic calls."
    )
    val pointsBasicCalls: BasicCallsResponse,

    @field:Schema(
        description = "The points for the under calls."
    )
    val pointsUnderCalls: UnderCallsDto,

    @field:Schema(
        description = "The points based on the levels the score of the looser is below."
    )
    val pointsLostScore: LostScoreResponse,

    @field:Schema(
        description = "The points based on special rules."
    )
    val pointsForSpecial: SpecialPointsResponse,
) {
    constructor(entity: ResultEntity) : this(
        id = entity.id,
        roundId = entity.round.id,
        team = entity.team,
        tricksCount = entity.trickCount,
        score = entity.score,
        targetScore = entity.target,
        pointsBasic = BasicPointsResponse(entity),
        pointsBasicCalls = BasicCallsResponse(entity),
        pointsUnderCalls = UnderCallsDto(entity),
        pointsLostScore = LostScoreResponse(entity),
        pointsForSpecial = SpecialPointsResponse(entity)
    )
}