package game.doppelkopf.adapter.graphql.core.game.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.adapter.graphql.common.StartedEnded
import game.doppelkopf.adapter.persistence.model.game.GameEntity

data class Game(
    val id: String,
    val playerLimit: Int,
    val cu: CreatedUpdated,
    val se: StartedEnded,
) {
    constructor(entity: GameEntity) : this(
        id = entity.id.toString(),
        playerLimit = entity.maxNumberOfPlayers,
        cu = CreatedUpdated(entity.created, entity.updated),
        se = StartedEnded(entity.started, entity.ended)
    )
}
