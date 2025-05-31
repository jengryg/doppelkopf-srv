package game.doppelkopf.adapter.graphql.core.round.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.adapter.graphql.common.StartedEnded
import game.doppelkopf.domain.round.enums.RoundContractPublic
import game.doppelkopf.domain.round.enums.RoundState
import game.doppelkopf.utils.Teamed
import java.util.*

data class RoundResponse(
    val id: UUID,
    val number: Int,
    val contract: RoundContractPublic,
    val state: RoundState,
    val result: Teamed<ResultResponse>?,
    val cu: CreatedUpdated,
    val se: StartedEnded,
)