package game.doppelkopf.adapter.api.core.call.dto

import game.doppelkopf.domain.call.enums.CallType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "Create a new call for the hand."
)
class CallCreateDto(
    @field:Schema(
        description = "The type of the call to create."
    )
    val callType: CallType
)