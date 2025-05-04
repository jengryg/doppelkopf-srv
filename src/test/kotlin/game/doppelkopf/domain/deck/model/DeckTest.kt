package game.doppelkopf.domain.deck.model

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.domain.deck.errors.InvalidCardException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

class DeckTest : BaseUnitTest() {
    @ParameterizedTest
    @EnumSource(value = DeckMode::class)
    fun `instantiate deck for each mode`(deckMode: DeckMode) {
        val deck = Deck.create(deckMode)

        assertThat(deck.mode).isEqualTo(deckMode)
        assertThat(deck.cards).hasSize(48)

        val hands = deck.dealHandCards()

        // TODO: seeded randomness
        val cards = hands.first.plus(hands.second).plus(hands.third).plus(hands.fourth)

        assertThat(cards).containsExactlyInAnyOrderElementsOf(deck.cards.values)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " ", "9H", "10H", "TH3", "XY1", "CQ0", "1QD", "AS1 "])
    fun `getting a card with invalid encoding should throw exception`(enc: String) {
        val deck = Deck.create(DeckMode.DIAMONDS)

        val result = deck.getCard(enc)

        assertThat(result.isFailure).isTrue
        assertThat(result.exceptionOrNull())
            .isInstanceOf(InvalidCardException::class.java)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " ", "9H", "10H", "TH3", "XY1", "CQ0", "1QD", "AS1 "])
    fun `getting list of cards with invalid encoding should throw exception`(enc: String) {
        val deck = Deck.create(DeckMode.DIAMONDS)

        val result = deck.getCards(listOf("AS1", "TH0", enc, "QC1"))

        assertThat(result.isFailure).isTrue
        assertThat(result.exceptionOrNull())
            .isInstanceOf(InvalidCardException::class.java)
    }


    @ParameterizedTest
    @ValueSource(strings = ["TH0", "AS1", "JD0", "KC1", "AD0"])
    fun `get card from deck with valid encoding returns from map`(enc: String) {
        val deck = Deck.create(DeckMode.DIAMONDS)

        val result = deck.getCard(enc)

        assertThat(result.isSuccess).isTrue
        assertThat(result.getOrNull()).isSameAs(deck.cards[enc])
    }
}