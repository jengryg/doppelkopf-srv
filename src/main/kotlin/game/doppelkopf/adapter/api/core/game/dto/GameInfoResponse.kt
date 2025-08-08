package game.doppelkopf.adapter.api.core.game.dto

import game.doppelkopf.adapter.api.core.player.dto.PlayerInfoResponse
import game.doppelkopf.adapter.api.user.dto.PublicUserInfoResponse
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.domain.game.enums.GameState
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

@Schema(
    description = "Information about a game of Doppelkopf.",
)
class GameInfoResponse(
    @field:Schema(
        description = "The UUID of this game."
    )
    val id: UUID,

    @field:Schema(
        description = "How many players are at most allowed to join this game."
    )
    val playerLimit: Int,

    @field:Schema(
        description = "The game was created at this moment."
    )
    val created: Instant,

    @field:Schema(
        description = "The game was started at this moment. If this is null, the game was not started yet."
    )
    val started: Instant?,

    @field:Schema(
        description = "The game was ended at this moment. If this is null, the game was not ended yet."
    )
    val ended: Instant?,

    @field:Schema(
        description = "The user that created this game."
    )
    val creator: PublicUserInfoResponse,

    @field:Schema(
        description = "The users that have already joined this game."
    )
    val players: List<PlayerInfoResponse>,

    @field:Schema(
        description = "The current state of this game."
    )
    val state: GameState,
) {
    constructor(entity: GameEntity) : this(
        id = entity.id,
        playerLimit = entity.maxNumberOfPlayers,
        created = entity.created,
        started = entity.started,
        ended = entity.ended,
        creator = PublicUserInfoResponse(entity.creator),
        players = entity.players.map { PlayerInfoResponse(it) },
        state = entity.state,
    )
}