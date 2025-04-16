package game.doppelkopf.api.core.dto.game

import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.validator.constraints.Range

@Schema(
    description = "Send this to create a new game."
)
class GameCreateDto(
    @Schema(
        description = "How many players are at most allowed to join the game."
    )
    @field:Range(min = 4, max = 8)
    val playerLimit: Int
)