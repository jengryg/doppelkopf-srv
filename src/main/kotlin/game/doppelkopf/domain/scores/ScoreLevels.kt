package game.doppelkopf.domain.scores

import game.doppelkopf.domain.hand.enums.DefiniteTeam
import game.doppelkopf.domain.hand.enums.Team

data class ScoreLevels(
    val p90: Team,
    val p60: Team,
    val p30: Team,
    val p00: Team
) {
    fun getPointsFor(team: DefiniteTeam): Int {
        return listOf(p90, p60, p30, p00).count { it == team.internal }
    }
}