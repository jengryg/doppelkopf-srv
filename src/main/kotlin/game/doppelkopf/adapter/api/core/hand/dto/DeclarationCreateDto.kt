package game.doppelkopf.adapter.api.core.hand.dto

import game.doppelkopf.domain.hand.enums.DeclarationOption
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Make a declaration for the hand.",
)
class DeclarationCreateDto(
    @field:Schema(
        description = "The declaration to make."
    )
    val declaration: DeclarationOption
)