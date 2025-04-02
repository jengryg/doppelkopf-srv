package game.doppelkopf.api.play.dto

import game.doppelkopf.core.play.enums.Bidding
import game.doppelkopf.core.play.enums.PublicDeclaration
import game.doppelkopf.core.play.enums.Team
import game.doppelkopf.persistence.play.HandEntity
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(
    description = "Information about a hand in a game of Doppelkopf that is ok for all users to see at any time."
)
class HandPublicInfoDto(
    @Schema(
        description = "The UUID of this hand."
    )
    val id: UUID,

    @Schema(
        description = "The UUID of the round this hand is played in."
    )
    val roundId: UUID,

    @Schema(
        description = "The UUID of the player this hand is played by."
    )
    val playerId: UUID,

    @Schema(
        description = "The declaration of this hand."
    )
    val declared: PublicDeclaration,

    @Schema(
        description = "The bid of this hand."
    )
    val bid: Bidding,

    @Schema(
        description = "The team this hands plays on, from the viewpoint of other players in the game.",
    )
    val publicTeam: Team,
) {
    constructor(handEntity: HandEntity) : this(
        id = handEntity.id,
        roundId = handEntity.round.id,
        playerId = handEntity.player.id,
        declared = handEntity.declared.publicDeclaration,
        bid = handEntity.bidding,
        publicTeam = handEntity.publicTeam,
    )
}