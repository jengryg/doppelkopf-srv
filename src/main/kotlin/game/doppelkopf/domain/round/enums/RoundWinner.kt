package game.doppelkopf.domain.round.enums

enum class RoundWinner {
    NA,
    RE,
    KO,
    DRAW
}

enum class DefiniteRoundWinner(
    val internal: RoundWinner
) {
    RE(RoundWinner.RE),
    KO(RoundWinner.KO),
    DRAW(RoundWinner.DRAW)
}