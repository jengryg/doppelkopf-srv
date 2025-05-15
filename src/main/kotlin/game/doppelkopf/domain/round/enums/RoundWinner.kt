package game.doppelkopf.domain.round.enums

import game.doppelkopf.domain.hand.enums.Team

enum class RoundWinner {
    NA,
    RE,
    KO,
    DRAW
}

enum class DefiniteRoundWinner(
    val internal: RoundWinner,
    val team: Team
) {
    RE(RoundWinner.RE, Team.RE),
    KO(RoundWinner.KO, Team.KO),
    DRAW(RoundWinner.DRAW, Team.NA)
}