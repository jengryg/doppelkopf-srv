package game.doppelkopf.domain.result.model

import game.doppelkopf.domain.hand.enums.DefiniteTeam

interface IResultProperties {
    val team: DefiniteTeam
    
    val trickCount: Int
    
    val score: Int
    val target: Int

    val pointsForWinning: Int

    val pointsForOpposition: Int

    val pointsForScore090: Int
    val pointsForScore060: Int
    val pointsForScore030: Int
    val pointsForScore000: Int

    val pointsForDoppelkopf: Int

    val pointsForCharly: Int
}