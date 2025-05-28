package game.doppelkopf.adapter.graphql.core.call.dto

import game.doppelkopf.domain.call.enums.CallType
import java.util.*

data class MakeCallInput(
    val handId: UUID,
    val callType: CallType,
)
