package game.doppelkopf.adapter.graphql.core.hand.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.adapter.graphql.core.UuidResponse
import game.doppelkopf.adapter.graphql.core.player.dto.PlayerTreeResponse
import game.doppelkopf.domain.hand.enums.BiddingPublic
import game.doppelkopf.domain.hand.enums.DeclarationPublic
import game.doppelkopf.domain.hand.enums.Team
import java.util.*

data class PublicHandTreeResponse(
    val id: UUID,
    val team: Team,
    val declared: DeclarationPublic,
    val bid: BiddingPublic,
    val cu: CreatedUpdated,
    val round: UuidResponse,
    val player: PlayerTreeResponse,
)
