package game.doppelkopf.api.core.dto.trick

import game.doppelkopf.core.common.enums.TrickOperation
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Operation to execute on a trick."
)
class TrickOperationDto(
    val op: TrickOperation
)