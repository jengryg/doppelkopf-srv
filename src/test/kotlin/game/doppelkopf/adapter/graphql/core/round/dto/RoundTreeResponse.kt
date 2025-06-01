package game.doppelkopf.adapter.graphql.core.round.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.adapter.graphql.common.StartedEnded
import game.doppelkopf.adapter.graphql.core.UuidResponse
import game.doppelkopf.adapter.graphql.core.call.dto.CallTreeResponse
import game.doppelkopf.adapter.graphql.core.hand.dto.PrivateHandTreeResponse
import game.doppelkopf.adapter.graphql.core.hand.dto.PublicHandTreeResponse
import game.doppelkopf.adapter.graphql.core.player.dto.PlayerTreeResponse
import game.doppelkopf.adapter.graphql.core.trick.dto.TrickTreeResponse
import game.doppelkopf.domain.round.enums.RoundContractPublic
import game.doppelkopf.domain.round.enums.RoundState
import game.doppelkopf.utils.Teamed
import java.util.*

data class RoundTreeResponse(
    val id: UUID,
    val number: Int,
    val contract: RoundContractPublic,
    val state: RoundState,
    val result: Teamed<ResultResponse>?,
    val cu: CreatedUpdated,
    val se: StartedEnded,
    val game: UuidResponse,
    val dealer: PlayerTreeResponse,
    val hand: PrivateHandTreeResponse?,
    val hands: List<PublicHandTreeResponse>,
    val calls: List<CallTreeResponse>,
    val tricks: List<TrickTreeResponse>,
    val currentTrick: TrickTreeResponse?,
)
