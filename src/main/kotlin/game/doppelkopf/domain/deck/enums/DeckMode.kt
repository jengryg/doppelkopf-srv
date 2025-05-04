package game.doppelkopf.domain.deck.enums

/**
 * The default DeckMode of a round of Doppelkopf is [DIAMONDS], but depending on the result of the pre-round auction
 * phase, players can modify the [DeckMode] of the round influencing the rankings of the cards in the deck.
 */
enum class DeckMode {
    // Suit based colored cards.
    DIAMONDS,
    HEARTS,
    SPADES,
    CLUBS,

    // Kind based colored cards.
    QUEENS,
    JACKS,

    // Color free.
    FREE
}