package game.doppelkopf.api.user.dto

import game.doppelkopf.persistence.model.user.UserEntity
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(
    description = "Representation of a user to be exposed to other users."
)
class PublicUserInfoDto(
    @Schema(
        description = "The UUID of the user."
    )
    val id: UUID,

    @Schema(
        description = "The name of the user."
    )
    val name: String,
) {
    constructor(entity: UserEntity) : this(
        id = entity.id,
        name = entity.username
    )
}