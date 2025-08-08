package game.doppelkopf.adapter.api.core.game.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.Range

@Schema(
    description = "Send this to create a new game."
)
class GameCreateRequest(
    @field:Schema(
        description = "How many players are at most allowed to join the game."
    )
    @field:Range(min = 4, max = 8)
    val playerLimit: Int,

    @field:Schema(
        description = "The seed to use for the game encoded as Base64. If null, a random seed is generated."
    )
    @field:Size(max = 256)
    val seed: ByteArray? = null,
)