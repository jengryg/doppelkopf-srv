package game.doppelkopf.api.game.dto

import game.doppelkopf.core.game.enums.GameOperation
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Operation to execute on a game."
)
class GameOperationDto(
    val op: GameOperation
)