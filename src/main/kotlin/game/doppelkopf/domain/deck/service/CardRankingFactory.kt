package game.doppelkopf.domain.deck.service

import game.doppelkopf.domain.deck.model.CardKind
import game.doppelkopf.domain.deck.model.CardSuit
import game.doppelkopf.domain.deck.model.DeckMode

object CardRankingFactory {
    /**
     * Define the general ranks of the suits: CLUBS > SPADES > HEARTS > DIAMOND
     */
    private val suitRank = listOf(CardSuit.CLUBS, CardSuit.SPADES, CardSuit.HEARTS, CardSuit.DIAMOND)

    /**
     * Define the general ranks of the kinds: ACE > TEN > KING > QUEEN > JACK > NINE
     */
    private val kindRank =
        listOf(CardKind.ACE, CardKind.TEN, CardKind.KING, CardKind.QUEEN, CardKind.JACK, CardKind.NINE)

    /**
     * Create a ranking configuration where no colored cards are considered.
     *
     * The [Int] in the value part of the map is the [game.doppelkopf.domain.deck.model.Card.ranking] value, the lower it is
     * the higher is the ranking of the card, i.e. cards with smaller numbers beats card with higher number.
     */
    private fun noColor(): MutableMap<String, Int> {
        return mutableMapOf<String, Int>().also {
            suitRank.forEach { suit ->
                // add un-colored kinds and suits in order given by kind ranking
                // the suit ranking does not count in this case here
                kindRank.forEachIndexed { index, kind ->
                    it["${kind.symbol}${suit.symbol}"] = 400 + index
                }
            }
        }
    }

    /**
     * Create a ranking configuration where colored cards are given by the [coloredSuit].
     * This configuration is used if the colored cards of the deck are determined by a [CardSuit].
     *
     * The [Int] in the value part of the map is the [game.doppelkopf.domain.deck.model.Card.ranking] value, the lower it is
     * the higher is the ranking of the card, i.e. cards with smaller numbers beats card with higher number.
     */
    private fun coloredSuit(coloredSuit: CardSuit): MutableMap<String, Int> {
        return noColor().also {
            kindRank.forEachIndexed { index, kind ->
                it["${kind.symbol}${coloredSuit.symbol}"] = 300 + index
                // improve the ranking of all kinds of the colored suits while preserving the kind ranking
            }

            suitRank.forEachIndexed { index, suit ->
                it["${CardKind.QUEEN.symbol}${suit.symbol}"] = 100 + index
                // improve the ranking of all queens while preserving their suit ranking
                it["${CardKind.JACK.symbol}${suit.symbol}"] = 200 + index
                // improve the ranking of all jacks while preserving their suit ranking
            }

            it["${CardKind.TEN.symbol}${CardSuit.HEARTS.symbol}"] = 1
            // improve the ranking of the Dullen (ten of hearts) and make them the highest card
        }
    }

    /**
     * Create a ranking configuration where colored cards are given by the [coloredKind].
     * This configuration is used if the colored cards of the decks are determined by a [CardKind].
     *
     * The [Int] in the value part of the map is the [game.doppelkopf.domain.deck.model.Card.ranking] value, the lower it is
     * the higher is the ranking of the card, i.e. cards with smaller numbers beats card with higher number.
     */
    private fun coloredKind(coloredKind: CardKind): MutableMap<String, Int> {
        return noColor().also {
            suitRank.forEachIndexed { index, suit ->
                it["${coloredKind.symbol}${suit.symbol}"] = 100 + index
                // improve the ranking of all cards of the coloredKind while preserving their suit ranking
            }
        }
    }

    /**
     * Returns the ranking configuration for the given [deckMode].
     *
     * The [Int] in the value part of the map is the [game.doppelkopf.domain.deck.model.Card.ranking] value, the lower it is
     * the higher is the ranking of the card, i.e. cards with smaller numbers beats card with higher number.
     */
    fun createRanking(deckMode: DeckMode): Map<String, Int> {
        return when (deckMode) {
            // no color variant
            DeckMode.FREE -> noColor()

            // suit determines colored cards
            DeckMode.DIAMONDS -> coloredSuit(CardSuit.DIAMOND)
            DeckMode.HEARTS -> coloredSuit(CardSuit.HEARTS)
            DeckMode.SPADES -> coloredSuit(CardSuit.SPADES)
            DeckMode.CLUBS -> coloredSuit(CardSuit.CLUBS)

            // kind determines colored cards
            DeckMode.QUEENS -> coloredKind(CardKind.QUEEN)
            DeckMode.JACKS -> coloredKind(CardKind.JACK)
        }.toMap()
    }
}