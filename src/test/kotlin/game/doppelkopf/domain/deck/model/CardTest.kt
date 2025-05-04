package game.doppelkopf.domain.deck.model

import game.doppelkopf.BaseUnitTest
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream

class CardTest : BaseUnitTest() {

    @ParameterizedTest
    @ValueSource(ints = [-1, 2, 3, 17, -42])
    fun `copy number not 0 or 1 trows exception`(num: Int) {
        assertThatThrownBy {
            Card(CardKind.ACE, CardSuit.SPADES, num, 0)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1])
    fun `copy number 0 and 1 are fine`(num: Int) {
        assertThatCode {
            Card(CardKind.ACE, CardSuit.SPADES, num, 0)
        }.doesNotThrowAnyException()
    }

    @ParameterizedTest(name = "{0} {1} {2} {3}")
    @MethodSource("provideCardsForPropertyCheck")
    fun `instantiate special and general cards and check their calculated properties`(
        kind: CardKind,
        suit: CardSuit,
        ranking: Int,
        isColored: Boolean,
        isQueenOfClubs: Boolean,
        isCharly: Boolean,
        isFox: Boolean,
        isNonColoredHearts: Boolean,
        satisfiesDemand: CardDemand
    ) {
        Card(kind, suit, 0, ranking).also {
            assertThat(it.isColored).isEqualTo(isColored)
            assertThat(it.isQueenOfClubs).isEqualTo(isQueenOfClubs)
            assertThat(it.isCharly).isEqualTo(isCharly)
            assertThat(it.isFox).isEqualTo(isFox)
            assertThat(it.isNonColoredHearts).isEqualTo(isNonColoredHearts)
            assertThat(it.demand).isEqualTo(satisfiesDemand)
        }
    }

    @Suppress("unused")
    private fun provideCardsForPropertyCheck(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(CardKind.ACE, CardSuit.SPADES, 399, true, false, false, false, false, CardDemand.COLORED),
            Arguments.of(CardKind.ACE, CardSuit.SPADES, 400, false, false, false, false, false, CardDemand.SPADES),
            Arguments.of(CardKind.QUEEN, CardSuit.SPADES, 400, false, false, false, false, false, CardDemand.SPADES),
            Arguments.of(CardKind.QUEEN, CardSuit.CLUBS, 999, false, true, false, false, false, CardDemand.CLUBS),
            Arguments.of(CardKind.JACK, CardSuit.SPADES, 111, true, false, false, false, false, CardDemand.COLORED),
            Arguments.of(CardKind.JACK, CardSuit.CLUBS, 999, false, false, true, false, false, CardDemand.CLUBS),
            Arguments.of(CardKind.ACE, CardSuit.DIAMOND, 777, false, false, false, true, false, CardDemand.DIAMOND),
            Arguments.of(CardKind.TEN, CardSuit.HEARTS, 100, true, false, false, false, false, CardDemand.COLORED),
            Arguments.of(CardKind.NINE, CardSuit.HEARTS, 400, false, false, false, false, true, CardDemand.HEARTS)
        )
    }

    @ParameterizedTest(name = "{2}")
    @MethodSource("provideCardsForEncoding")
    fun `check encoding of cards`(
        kind: CardKind,
        suit: CardSuit,
        ksEnc: String
    ) {
        assertThat(Card(kind, suit, 0, 0).encoded).isEqualTo("${ksEnc}0")
        assertThat(Card(kind, suit, 1, 0).encoded).isEqualTo("${ksEnc}1")
    }

    @Suppress("unused")
    private fun provideCardsForEncoding(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(CardKind.ACE, CardSuit.SPADES, "AS"),
            Arguments.of(CardKind.QUEEN, CardSuit.SPADES, "QS"),
            Arguments.of(CardKind.JACK, CardSuit.SPADES, "JS"),
            Arguments.of(CardKind.TEN, CardSuit.HEARTS, "TH"),
            Arguments.of(CardKind.NINE, CardSuit.HEARTS, "9H"),
            Arguments.of(CardKind.KING, CardSuit.HEARTS, "KH"),
            Arguments.of(CardKind.ACE, CardSuit.DIAMOND, "AD"),
            Arguments.of(CardKind.QUEEN, CardSuit.CLUBS, "QC")
        )
    }
}