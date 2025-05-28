package game.doppelkopf.adapter.graphql.common

import game.doppelkopf.adapter.persistence.model.IBaseEntity
import java.time.Instant

data class CreatedUpdated(
    val created: Instant,
    val updated: Instant,
) {
    constructor(entity: IBaseEntity) : this(
        created = entity.created,
        updated = entity.updated,
    )
}
