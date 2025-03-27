package game.doppelkopf.api.game.dto

import game.doppelkopf.api.user.dto.PublicUserInfoDto
import game.doppelkopf.persistence.game.PlayerEntity
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(
    description = "A user playing in a game as player.",
)
class PlayerInfoDto(
    @Schema(
        description = "The UUID of this player."
    )
    val id: UUID,

    @Schema(
        description = "The number of the seat this player is sitting on in the game.",
    )
    val seat: Int,

    @Schema(
        description = "The user that is playing in the game as this player.",
    )
    val user: PublicUserInfoDto
) {
    constructor(entity: PlayerEntity) : this(
        id = entity.id,
        seat = entity.seat,
        user = PublicUserInfoDto(entity.user)
    )
}