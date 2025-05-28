package game.doppelkopf.adapter.graphql.user.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import java.util.*

data class PrivateUser(
    val id: UUID,
    val name: String,
    val cu: CreatedUpdated
) {
    constructor(entity: UserEntity) : this(
        id = entity.id,
        name = entity.username,
        cu = CreatedUpdated(created = entity.created, updated = entity.updated)
    )
}