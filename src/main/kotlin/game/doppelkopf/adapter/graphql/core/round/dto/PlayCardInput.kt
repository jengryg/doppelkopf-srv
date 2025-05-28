package game.doppelkopf.adapter.graphql.core.round.dto

import java.util.UUID

data class PlayCardInput(
    val roundId: UUID,
    val card: String,
)