package game.doppelkopf.adapter.graphql.core.hand.dto

import game.doppelkopf.domain.hand.enums.Team
import java.util.UUID

data class PrivateHandResponse(
    val id: UUID,
    val team: Team,
    val cardsPlayed: List<String>,
    val cardsRemaining: List<String>,
    val public: PublicHandResponse,
)