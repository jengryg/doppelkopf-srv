package game.doppelkopf.domain.deck.enums

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
}