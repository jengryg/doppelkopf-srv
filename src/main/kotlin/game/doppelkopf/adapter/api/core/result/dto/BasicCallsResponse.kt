package game.doppelkopf.adapter.api.core.result.dto

import game.doppelkopf.adapter.persistence.model.result.ResultEntity
import io.swagger.v3.oas.annotations.media.Schema

class BasicCallsResponse(
    @field:Schema(
        description = "2 points for the winner if RE was called."
    )
    val re: Int,

    @field:Schema(
        description = "2 points for the winner if KO was called."
    )
    val ko: Int
) {
    constructor(entity: ResultEntity) : this(
        re = entity.pointsBasicCallsRe,
        ko = entity.pointsBasicCallsKo
    )
}