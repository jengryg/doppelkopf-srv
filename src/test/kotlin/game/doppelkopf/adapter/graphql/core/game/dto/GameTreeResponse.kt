package game.doppelkopf.adapter.graphql.core.game.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.adapter.graphql.common.StartedEnded
import game.doppelkopf.adapter.graphql.core.player.dto.PlayerTreeResponse
import game.doppelkopf.adapter.graphql.core.round.dto.RoundTreeResponse
import game.doppelkopf.domain.game.enums.GameState
import java.util.UUID

data class GameTreeResponse(
    val id: UUID,
    val playerLimit: Int,
    val state: GameState,
    val cu: CreatedUpdated,
    val se: StartedEnded,
    val players: List<PlayerTreeResponse>,
    val player: PlayerTreeResponse?,
    val rounds: List<RoundTreeResponse>
)
