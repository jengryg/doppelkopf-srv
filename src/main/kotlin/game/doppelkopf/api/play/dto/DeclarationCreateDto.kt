package game.doppelkopf.api.play.dto

import game.doppelkopf.core.play.enums.DeclarationOption
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Make a declaration for the hand.",
)
class DeclarationCreateDto(
    @Schema(
        description = "The declaration to make."
    )
    val declaration: DeclarationOption
)