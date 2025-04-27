package game.doppelkopf.api.core.dto.turn

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern

@Schema(
    description = "Send this to play a card."
)
class CreateTurnDto(
    @Schema(
        description = "The card you want to play. Must be one of the cards on your hand. Encoded as {kind symbol}{suit symbol}{0 or 1}."
    )
    @field:Pattern(regexp = "^[AKQJT9][DHSC][01]$")
    val card: String
)