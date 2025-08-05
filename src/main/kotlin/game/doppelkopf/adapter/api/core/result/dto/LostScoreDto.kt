package game.doppelkopf.adapter.api.core.result.dto

import game.doppelkopf.adapter.persistence.model.result.ResultEntity
import io.swagger.v3.oas.annotations.media.Schema

class LostScoreDto(
    @field:Schema(
        description = "1 point when the loosing team scores below 90."
    )
    val p90: Int,
    @field:Schema(
        description = "1 point when the loosing team scores below 60."
    )
    val p60: Int,
    @field:Schema(
        description = "1 point when the loosing team scores below 30."
    )
    val p30: Int,
    @field:Schema(
        description = "1 point when the loosing team scores no trick at all."
    )
    val p00: Int
) {
    constructor(entity: ResultEntity) : this(
        p90 = entity.pointsLostScore90,
        p60 = entity.pointsLostScore60,
        p30 = entity.pointsLostScore30,
        p00 = entity.pointsLostScore00,
    )
}