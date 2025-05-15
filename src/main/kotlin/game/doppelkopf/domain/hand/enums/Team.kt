package game.doppelkopf.domain.hand.enums

enum class Team {
    RE,
    KO,
    NA
}

enum class DefiniteTeam(val internal: Team) {
    RE(Team.RE),
    KO(Team.KO);

    /**
     * @return 1 if this matches the [other], otherwise 0
     */
    fun oneIfMatches(other: Team): Int {
        return if (this.internal == other) 1 else 0
    }
}