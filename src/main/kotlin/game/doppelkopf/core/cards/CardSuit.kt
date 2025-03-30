package game.doppelkopf.core.cards

/**
 * Enum definition of the suits of cards for Doppelkopf.
 */
enum class CardSuit(val symbol: Char) {
    DIAMOND('D'),
    HEARTS('H'),
    SPADES('S'),
    CLUBS('C');


    companion object {
        fun getBySymbol(symbol: Char): CardSuit {
            return when (symbol) {
                'D' -> DIAMOND
                'H' -> HEARTS
                'S' -> SPADES
                'C' -> CLUBS
                else -> throw IllegalArgumentException("Unknown CardSuit symbol representation: $symbol")
            }
        }
    }
}