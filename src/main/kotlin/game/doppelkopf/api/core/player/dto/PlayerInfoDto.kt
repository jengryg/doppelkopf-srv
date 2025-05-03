package game.doppelkopf.api.core.player.dto

import game.doppelkopf.api.user.dto.PublicUserInfoDto
import game.doppelkopf.persistence.model.player.PlayerEntity
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
        description = "The UUID of the game this player is playing in.",
    )
    val gameId: UUID,

    @Schema(
        description = "The user that is playing in the game as this player.",
    )
    val user: PublicUserInfoDto,

    @Schema(
        description = "Is this player is the current dealer in the game."
    )
    val dealer: Boolean
) {
    constructor(entity: PlayerEntity) : this(
        id = entity.id,
        seat = entity.seat,
        gameId = entity.game.id,
        user = PublicUserInfoDto(entity.user),
        dealer = entity.dealer
    )
}