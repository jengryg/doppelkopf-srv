package game.doppelkopf.api.core.dto.game

import game.doppelkopf.core.common.enums.GameOperation
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Operation to execute on a game."
)
class GameOperationDto(
    val op: GameOperation
)