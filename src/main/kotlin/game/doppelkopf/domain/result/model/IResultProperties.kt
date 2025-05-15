package game.doppelkopf.domain.result.model

import game.doppelkopf.domain.hand.enums.DefiniteTeam

interface IResultProperties {
    val team: DefiniteTeam

    val trickCount: Int

    val score: Int
    val target: Int

    // basic points
    val pointsForWinning: Int

    // lostScore points
    val pointsLostScore90: Int
    val pointsLostScore60: Int
    val pointsLostScore30: Int
    val pointsLostScore00: Int

    // basicCalls points
    val pointsBasicCallsRe: Int
    val pointsBasicCallsKo: Int

    // under calls points
    val pointsUnderCallsRe90: Int
    val pointsUnderCallsKo90: Int
    val pointsUnderCallsRe60: Int
    val pointsUnderCallsKo60: Int
    val pointsUnderCallsRe30: Int
    val pointsUnderCallsKo30: Int
    val pointsUnderCallsRe00: Int
    val pointsUnderCallsKo00: Int

    // beating calls of opposite party points
    val pointsBeatingRe90: Int
    val pointsBeatingKo90: Int
    val pointsBeatingRe60: Int
    val pointsBeatingKo60: Int
    val pointsBeatingRe30: Int
    val pointsBeatingKo30: Int
    val pointsBeatingRe00: Int
    val pointsBeatingKo00: Int

    // special points
    val pointsForOpposition: Int
    val pointsForDoppelkopf: Int
    val pointsForCharly: Int
}