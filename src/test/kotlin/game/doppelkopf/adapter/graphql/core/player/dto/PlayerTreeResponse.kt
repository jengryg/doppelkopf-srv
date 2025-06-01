package game.doppelkopf.adapter.graphql.core.player.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.adapter.graphql.core.UuidResponse
import game.doppelkopf.adapter.graphql.user.dto.PublicUserResponse
import java.util.UUID

data class PlayerTreeResponse(
    val id: UUID,
    val seat: Int,
    val dealer: Boolean,
    val cu: CreatedUpdated,
    val game: UuidResponse,
    val user: PublicUserResponse,
)
