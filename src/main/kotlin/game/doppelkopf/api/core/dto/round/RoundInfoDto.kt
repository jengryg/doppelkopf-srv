package game.doppelkopf.api.core.dto.round

import game.doppelkopf.api.core.dto.player.PlayerInfoDto
import game.doppelkopf.core.common.enums.RoundContractPublic
import game.doppelkopf.core.common.enums.RoundState
import game.doppelkopf.persistence.model.round.RoundEntity
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

@Schema(
    description = "Information about a round in a game of Doppelkopf.",
)
class RoundInfoDto(
    @Schema(
        description = "The UUID of this round."
    )
    val id: UUID,

    @Schema(
        description = "The UUID of the game this round is played in.",
    )
    val gameId: UUID,

    @Schema(
        description = "The player that dealt this round.r"
    )
    val dealer: PlayerInfoDto,

    @Schema(
        description = "The number of this round, incrementally starting at 1."
    )
    val number: Int,

    @Schema(
        description = "The current state of the round."
    )
    val state: RoundState,

    @Schema(
        description = "The public contract information of the round."
    )
    val contract: RoundContractPublic,
) {
    constructor(roundEntity: RoundEntity) : this(
        id = roundEntity.id,
        gameId = roundEntity.game.id,
        dealer = PlayerInfoDto(roundEntity.dealer),
        number = roundEntity.number,
        state = roundEntity.state,
        contract = roundEntity.contract.roundContractPublic
    )
}