package game.doppelkopf.adapter.graphql.core.player.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import java.util.*

data class PlayerResponse(
    val id: UUID,
    val seat: Int,
    val dealer: Boolean,
    val cu: CreatedUpdated,
)