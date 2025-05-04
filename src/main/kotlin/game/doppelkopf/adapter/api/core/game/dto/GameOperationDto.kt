package game.doppelkopf.adapter.api.core.game.dto

import game.doppelkopf.domain.game.enums.GameOperation
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Operation to execute on a game."
)
class GameOperationDto(
    val op: GameOperation
)