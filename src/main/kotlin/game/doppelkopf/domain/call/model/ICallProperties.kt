package game.doppelkopf.domain.call.model

import game.doppelkopf.domain.call.enums.CallType

interface ICallProperties {
    val callType: CallType
    val cardsPlayedBefore: Int
}