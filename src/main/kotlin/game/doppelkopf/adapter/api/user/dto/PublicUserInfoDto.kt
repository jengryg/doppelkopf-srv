package game.doppelkopf.adapter.api.user.dto

import game.doppelkopf.adapter.persistence.model.user.UserEntity
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(
    description = "Representation of a user to be exposed to other users."
)
class PublicUserInfoDto(
    @field:Schema(
        description = "The UUID of the user."
    )
    val id: UUID,

    @field:Schema(
        description = "The name of the user."
    )
    val name: String,
) {
    constructor(entity: UserEntity) : this(
        id = entity.id,
        name = entity.username
    )
}