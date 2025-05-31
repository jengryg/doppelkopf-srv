package game.doppelkopf.adapter.graphql.user.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import java.util.*

data class PrivateUserResponse(
    val id: UUID,
    val name: String,
    val cu: CreatedUpdated,
)