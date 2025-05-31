package game.doppelkopf.adapter.graphql.core.turn.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import java.util.*

data class TurnResponse(
    val id: UUID,
    val cu: CreatedUpdated,
    val number: Int,
    val card: String,
)
