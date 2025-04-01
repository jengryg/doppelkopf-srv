package game.doppelkopf.core.cards

import game.doppelkopf.utils.Quadruple

/**
 * Use [Deck.create] to instantiate a new [Deck] for the given [DeckMode].
 * Uses [CardRankingFactory] to create a ranking map based on the [DeckMode] that determines the ranking of the cards
 * in this [Deck].
 */
class Deck private constructor(
    val mode: DeckMode,
    private val ranking: Map<String, Int>
) {
    val cards = CardKind.entries.flatMap { kind ->
        CardSuit.entries.flatMap { suit ->
            ranking["${kind.symbol}${suit.symbol}"]?.let {
                listOf(
                    Card(kind = kind, suit = suit, copy = 0, ranking = it),
                    Card(kind = kind, suit = suit, copy = 1, ranking = it),
                )
            } ?: throw IllegalArgumentException("No ranking for card $kind of $suit provided.")
        }
    }.also {
        if (it.size % 4 != 0) {
            throw IllegalArgumentException("The size of the deck is ${it.size}, which is not divisible by 4.")
        }
    }.associateBy { it.encoded }.toMap()

    /**
     * @return a [Quadruple] containing four [List] of [Card], one list for each player hand
     */
    fun dealHandCards(): Quadruple<List<Card>> {
        return cards.keys.shuffled()
            .map { cards[it]!! }.chunked(cards.size / 4).let {
                Quadruple(
                    it[0],
                    it[1],
                    it[2],
                    it[3],
                )
            }
    }

    fun getCards(encodings: List<String>): List<Card> {
        return encodings.map {
            cards[it] ?: throw IllegalArgumentException("Can not identify $it as a card.")
        }
    }

    fun getCard(encoding: String): Card {
        return cards[encoding] ?: throw IllegalArgumentException("Can not identify  $encoding as a card.")
    }

    companion object {
        fun create(deckMode: DeckMode): Deck {
            return Deck(
                mode = deckMode,
                ranking = CardRankingFactory.createRanking(deckMode)
            )
        }
    }
}