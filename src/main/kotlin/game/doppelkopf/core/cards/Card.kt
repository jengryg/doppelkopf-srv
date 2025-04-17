package game.doppelkopf.core.cards

/**
 * A card in the game of Doppelkopf.
 *
 * @param kind the raw kind of the card as it is in a pack of cards
 * @param suit the raw suit of the card as it is in a pack of cards
 * @param copy the copy number 0 or 1 since every combination of kind and suit exists twice
 * @param ranking the ranking index of this card, the lower the value, the better the card
 */
class Card(
    val kind: CardKind,
    val suit: CardSuit,
    val copy: Int,
    val ranking: Int,
) {
    init {
        require(copy == 0 || copy == 1) {
            "The card copy property must be either 0 or 1."
        }
    }

    /**
     * A 3 character long representation code for this card encoding it as `KSC`, i.e. [CardKind], [CardSuit] and the
     * copy number.
     */
    val encoded get() = "${kind.symbol}${suit.symbol}${copy}"

    val isColored = ranking < 400
    val isQueenOfClubs = kind == CardKind.QUEEN && suit == CardSuit.CLUBS
    val isCharly = kind == CardKind.JACK && suit == CardSuit.CLUBS
    val isFox = kind == CardKind.ACE && suit == CardSuit.DIAMOND
    val isNonColoredHearts = !isColored && suit == CardSuit.HEARTS

    val demand = when {
        isColored -> CardDemand.COLORED
        else -> when (suit) {
            CardSuit.DIAMOND -> CardDemand.DIAMOND
            CardSuit.HEARTS -> CardDemand.HEARTS
            CardSuit.CLUBS -> CardDemand.CLUBS
            CardSuit.SPADES -> CardDemand.SPADES
        }
    }

    override fun toString(): String {
        return "$kind of $suit"
    }
}