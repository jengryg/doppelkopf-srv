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
        description = "The basic points for this team."
    )
    val pointsBasic: BasicPointsDto,

    @Schema(
        description = "The points for the basic calls."
    )
    val pointsBasicCalls: BasicCallsDto,

    @Schema(
        description = "The points for the under calls."
    )
    val pointsUnderCalls: UnderCallsDto,

    @Schema(
        description = "The points based on the levels the score of the looser is below."
    )
    val pointsLostScore: LostScoreDto,

    @Schema(
        description = "The points based on special rules."
    )
    val pointsForSpecial: SpecialPointsDto,
) {
    constructor(entity: ResultEntity) : this(
        id = entity.id,
        roundId = entity.round.id,
        team = entity.team,
        tricksCount = entity.trickCount,
        score = entity.score,
        targetScore = entity.target,
        pointsBasic = BasicPointsDto(entity),
        pointsBasicCalls = BasicCallsDto(entity),
        pointsUnderCalls = UnderCallsDto(entity),
        pointsLostScore = LostScoreDto(entity),
        pointsForSpecial = SpecialPointsDto(entity)
    )
}