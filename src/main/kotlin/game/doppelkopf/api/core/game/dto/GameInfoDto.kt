package game.doppelkopf.api.core.game.dto

import game.doppelkopf.api.core.player.dto.PlayerInfoDto
import game.doppelkopf.api.user.dto.PublicUserInfoDto
import game.doppelkopf.persistence.model.game.GameEntity
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

@Schema(
    description = "Information about a game of Doppelkopf.",
)
class GameInfoDto(
    @Schema(
        description = "The UUID of this game."
    )
    val id: UUID,

    @Schema(
        description = "How many players are at most allowed to join this game."
    )
    val playerLimit: Int,

    @Schema(
        description = "The game was created at this moment."
    )
    val created: Instant,

    @Schema(
        description = "The game was started at this moment. If this is null, the game was not started yet."
    )
    val started: Instant?,

    @Schema(
        description = "The game was ended at this moment. If this is null, the game was not ended yet."
    )
    val ended: Instant?,

    @Schema(
        description = "The user that created this game."
    )
    val creator: PublicUserInfoDto,

    @Schema(
        description = "The users that have already joined this game."
    )
    val players: List<PlayerInfoDto>
) {
    constructor(entity: GameEntity) : this(
        id = entity.id,
        playerLimit = entity.maxNumberOfPlayers,
        created = entity.created,
        started = entity.started,
        ended = entity.ended,
        creator = PublicUserInfoDto(entity.creator),
        players = entity.players.map { PlayerInfoDto(it) }
    )
}