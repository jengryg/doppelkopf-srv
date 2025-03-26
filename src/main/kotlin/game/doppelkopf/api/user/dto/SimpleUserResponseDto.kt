package game.doppelkopf.api.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(
    description = "Simple information for a user."
)
class SimpleUserResponseDto(
    @Schema(
        description = "The id of the user."
    )
    val id: UUID,

    @Schema(
        description = "The username of the user."
    )
    val name: String,
)