package game.doppelkopf.adapter.api.core.trick.dto

import game.doppelkopf.domain.trick.enums.TrickOperation
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Operation to execute on a trick."
)
class TrickOperationRequest(
    val op: TrickOperation
)