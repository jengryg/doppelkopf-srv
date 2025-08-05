package game.doppelkopf.adapter.api.core.player.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.validator.constraints.Range

@Schema(
    description = "Join a game at the specified seat as player."
)
class PlayerCreateDto(
    @field:Schema(
        description = "The seat position to take."
    )
    @field:Range(min = 1, max = 7)
    val seat: Int,
)