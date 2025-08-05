package game.doppelkopf.adapter.api.core.result.dto

import game.doppelkopf.adapter.persistence.model.result.ResultEntity
import io.swagger.v3.oas.annotations.media.Schema

class BasicPointsDto(
    @field:Schema(
        description = "1 base point for the winner."
    )
    val winning: Int,
) {
    constructor(entity: ResultEntity) : this(
        winning = entity.pointsForWinning
    )
}