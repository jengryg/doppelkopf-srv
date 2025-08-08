package game.doppelkopf.adapter.api.core.result.dto

import game.doppelkopf.adapter.persistence.model.result.ResultEntity
import io.swagger.v3.oas.annotations.media.Schema

class SpecialPointsResponse(
    @field:Schema(
        description = "1 point if KO wins against RE."
    )
    val opposition: Int,

    @field:Schema(
        description = "1 point for each trick with a score of 40 or more."
    )
    val doppelkopf: Int,

    @field:Schema(
        description = "1 point for winning the last trick with Jack of Clubs."
    )
    val charly: Int,
) {
    constructor(entity: ResultEntity) : this(
        opposition = entity.pointsForOpposition,
        doppelkopf = entity.pointsForDoppelkopf,
        charly = entity.pointsForCharly,
    )
}