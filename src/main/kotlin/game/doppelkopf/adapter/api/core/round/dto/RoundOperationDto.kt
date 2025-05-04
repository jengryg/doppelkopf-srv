package game.doppelkopf.adapter.api.core.round.dto

import game.doppelkopf.domain.round.enums.RoundOperation
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Operation to execute on a round."
)
class RoundOperationDto(
    val op: RoundOperation
)