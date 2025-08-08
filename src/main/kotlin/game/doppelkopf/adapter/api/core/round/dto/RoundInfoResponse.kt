package game.doppelkopf.adapter.api.core.round.dto

import game.doppelkopf.adapter.api.core.player.dto.PlayerInfoResponse
import game.doppelkopf.adapter.api.core.result.dto.ResultInfoResponse
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.domain.round.enums.RoundContractPublic
import game.doppelkopf.domain.round.enums.RoundState
import game.doppelkopf.utils.Teamed
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

@Schema(
    description = "Information about a round in a game of Doppelkopf.",
)
class RoundInfoResponse(
    @field:Schema(
        description = "The UUID of this round."
    )
    val id: UUID,

    @field:Schema(
        description = "The UUID of the game this round is played in.",
    )
    val gameId: UUID,

    @field:Schema(
        description = "The player that dealt this round."
    )
    val dealer: PlayerInfoResponse,

    @field:Schema(
        description = "The number of this round, incrementally starting at 1."
    )
    val number: Int,

    @field:Schema(
        description = "The current state of the round."
    )
    val state: RoundState,

    @field:Schema(
        description = "The public contract information of the round."
    )
    val contract: RoundContractPublic,

    @field:Schema(
        description = "The result of this round if available, otherwise null."
    )
    val result: Teamed<ResultInfoResponse>?,

    @field:Schema(
        description = "The round was started at this moment. If this is null, the round was not started yet."
    )
    val started: Instant?,

    @field:Schema(
        description = "The round was ended at this moment. If this is null, the round was not ended yet."
    )
    val ended: Instant?,
) {
    constructor(roundEntity: RoundEntity) : this(
        id = roundEntity.id,
        gameId = roundEntity.game.id,
        dealer = PlayerInfoResponse(roundEntity.dealer),
        number = roundEntity.number,
        state = roundEntity.state,
        contract = roundEntity.contract.roundContractPublic,
        result = Teamed.from(roundEntity.results) { it.team.internal }?.map { ResultInfoResponse(it) },
        started = roundEntity.started,
        ended = roundEntity.ended,
    )
}