package game.doppelkopf.api.game.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.validator.constraints.Range
import java.util.*

@Schema(
    description = "Join the specified game id at the specified seat as player."
)
class PlayerCreateDto(
    @Schema(
        description = "The UUID of the game to join."
    )
    val gameId: UUID,

    @Schema(
        description = "The seat position to take."
    )
    @field:Range(min = 1, max = 7)
    val seat: Int,
)