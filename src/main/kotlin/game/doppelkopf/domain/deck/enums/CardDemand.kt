package game.doppelkopf.domain.deck.enums

/**
 * The first card of each trick defines the [CardDemand] that MUST be satisfied by a player if possible.
 */
enum class CardDemand {
    COLORED,
    DIAMOND,
    HEARTS,
    SPADES,
    CLUBS
}