package game.doppelkopf.adapter.api.core.hand.dto

import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.domain.hand.enums.BiddingPublic
import game.doppelkopf.domain.hand.enums.DeclarationPublic
import game.doppelkopf.domain.hand.enums.Team
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(
    description = "Information about a hand in a game of Doppelkopf that is ok for all users to see at any time."
)
class HandPublicInfoResponse(
    @field:Schema(
        description = "The UUID of this hand."
    )
    val id: UUID,

    @field:Schema(
        description = "The UUID of the round this hand is played in."
    )
    val roundId: UUID,

    @field:Schema(
        description = "The UUID of the player this hand is played by."
    )
    val playerId: UUID,

    @field:Schema(
        description = "The declaration of this hand."
    )
    val declared: DeclarationPublic,

    @field:Schema(
        description = "The bid of this hand."
    )
    val bid: BiddingPublic,

    @field:Schema(
        description = "The team this hands plays on, from the viewpoint of other players in the game.",
    )
    val publicTeam: Team,
) {
    constructor(handEntity: HandEntity) : this(
        id = handEntity.id,
        roundId = handEntity.round.id,
        playerId = handEntity.player.id,
        declared = handEntity.declared.declarationPublic,
        bid = handEntity.bidding.biddingPublic,
        publicTeam = handEntity.publicTeam,
    )
}