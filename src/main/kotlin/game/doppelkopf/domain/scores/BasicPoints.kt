package game.doppelkopf.domain.scores

import game.doppelkopf.domain.hand.enums.DefiniteTeam
import game.doppelkopf.domain.hand.enums.Team

data class BasicPoints(
    val winning: Team
) {
    fun getPointsFor(team: DefiniteTeam): Int {
        return if (winning == team.internal) 1 else 0
    }
}