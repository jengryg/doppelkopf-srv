package game.doppelkopf.core.cards

/**
 * Enum definition of the kinds of cards for Doppelkopf.
 */
enum class CardKind(
    val symbol: Char,
    val points: Int,
) {
    ACE('A', 11),
    TEN('T', 10),
    KING('K', 4),
    QUEEN('Q', 3),
    JACK('J', 2),
    NINE('9', 0);

    companion object {
        fun getBySymbol(symbol: Char): CardKind {
            return when (symbol) {
                'A' -> ACE
                'T' -> TEN
                'K' -> KING
                'Q' -> QUEEN
                'J' -> JACK
                '9' -> NINE
                else -> throw IllegalArgumentException("Unknown CardKind symbol representation: $symbol")
            }
        }
    }
}