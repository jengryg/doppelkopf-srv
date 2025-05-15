package game.doppelkopf.domain.scores

import game.doppelkopf.domain.hand.enums.DefiniteTeam
import game.doppelkopf.domain.hand.enums.Team

data class BasicCalls(
    val re: Team,
    val ko: Team
) {
    fun getPointsFor(team: DefiniteTeam): Int {
        // RE and KO are worth 2 points each.
        return 2 * listOf(re, ko).count { it == team.internal }
    }
}