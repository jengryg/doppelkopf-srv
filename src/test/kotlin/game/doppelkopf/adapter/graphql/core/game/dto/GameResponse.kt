package game.doppelkopf.adapter.graphql.core.game.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.adapter.graphql.common.StartedEnded
import game.doppelkopf.domain.game.enums.GameState
import java.util.*

data class GameResponse(
    val id: UUID,
    val playerLimit: Int,
    val state: GameState,
    val cu: CreatedUpdated,
    val se: StartedEnded,
)