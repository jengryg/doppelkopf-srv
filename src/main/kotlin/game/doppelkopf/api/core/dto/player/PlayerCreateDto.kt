package game.doppelkopf.api.core.dto.player

import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.validator.constraints.Range

@Schema(
    description = "Join a game at the specified seat as player."
)
class PlayerCreateDto(
    @Schema(
        description = "The seat position to take."
    )
    @field:Range(min = 1, max = 7)
    val seat: Int,
)