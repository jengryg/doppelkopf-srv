package game.doppelkopf.domain.hand.enums

enum class Team {
    RE,
    KO,
    NA
}

enum class DefiniteTeam(val internal: Team) {
    RE(Team.RE),
    KO(Team.KO)
}