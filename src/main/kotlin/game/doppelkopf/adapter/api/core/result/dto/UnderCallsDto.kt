package game.doppelkopf.adapter.api.core.result.dto

import game.doppelkopf.adapter.persistence.model.result.ResultEntity
import io.swagger.v3.oas.annotations.media.Schema

class UnderCallsDto(
    @Schema(
        description = "1 point for the winner for each level call made by RE team."
    )
    val re: UnderCallsTeamDto,

    @Schema(
        description = "1 point for the winner for each level call made by KO team."
    )
    val ko: UnderCallsTeamDto,
) {
    class UnderCallsTeamDto(
        @Schema(
            description = "under 90 call"
        )
        val p90: Int,

        @Schema(
            description = "under 60 call"
        )
        val p60: Int,

        @Schema(
            description = "under 30 call"
        )
        val p30: Int,

        @Schema(
            description = "no tricks at all call"
        )
        val p00: Int,
    )

    constructor(entity: ResultEntity) : this(
        re = UnderCallsTeamDto(
            p90 = entity.pointsUnderCallsRe90,
            p60 = entity.pointsUnderCallsRe60,
            p30 = entity.pointsUnderCallsRe30,
            p00 = entity.pointsUnderCallsRe00,
        ),
        ko = UnderCallsTeamDto(
            p90 = entity.pointsUnderCallsKo90,
            p60 = entity.pointsUnderCallsKo60,
            p30 = entity.pointsUnderCallsKo30,
            p00 = entity.pointsUnderCallsKo00,
        )
    )
}