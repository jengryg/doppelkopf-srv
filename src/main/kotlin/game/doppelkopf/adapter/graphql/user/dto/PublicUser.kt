package game.doppelkopf.adapter.graphql.user.dto

import game.doppelkopf.adapter.persistence.model.user.UserEntity
import java.util.*

data class PublicUser(
    val id: UUID,
    val name: String
) {
    constructor(entity: UserEntity) : this(
        id = entity.id,
        name = entity.username,
    )
}