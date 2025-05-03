package game.doppelkopf.api.core.round.dto

import game.doppelkopf.core.common.enums.RoundOperation
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Operation to execute on a round."
)
class RoundOperationDto(
    val op: RoundOperation
)