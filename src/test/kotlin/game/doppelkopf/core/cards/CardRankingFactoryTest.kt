package game.doppelkopf.core.cards

import game.doppelkopf.BaseUnitTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class CardRankingFactoryTest : BaseUnitTest() {

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideRankingArguments")
    fun `check generated rank ordering`(
        deckMode: DeckMode,
        expectedCardOrdering: ExpectedCardOrdering
    ) {
        val ranking = CardRankingFactory.createRanking(deckMode)

        val coloredCards =
            ranking.filter { it.value < 400 }.entries.sortedBy { it.value }
                .joinToString(" ") { it.key }

        Assertions.assertThat(coloredCards).isEqualTo(expectedCardOrdering.coloredCards)

        val noColoredCards = ranking.filter { it.value >= 400 }


        Assertions.assertThat(
            createRankingString(noColoredCards.filter { it.key.last() == 'D' })
        ).isEqualTo(expectedCardOrdering.noColorDiamond)

        Assertions.assertThat(
            createRankingString(noColoredCards.filter { it.key.last() == 'H' })
        ).isEqualTo(expectedCardOrdering.noColorHeart)

        Assertions.assertThat(
            createRankingString(noColoredCards.filter { it.key.last() == 'S' })
        ).isEqualTo(expectedCardOrdering.noColorSpades)

        Assertions.assertThat(
            createRankingString(noColoredCards.filter { it.key.last() == 'C' })
        ).isEqualTo(expectedCardOrdering.noColorClubs)
    }

    private fun createRankingString(rankedCards: Map<String, Int>): String {
        return rankedCards.entries.sortedBy { it.value }.joinToString(" ") { it.key }
    }

    @Suppress("unused")
    private fun provideRankingArguments(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(
                DeckMode.DIAMONDS,
                ExpectedCardOrdering(
                    coloredCards = "TH QC QS QH QD JC JS JH JD AD TD KD 9D",
                    noColorDiamond = "",
                    noColorHeart = "AH KH 9H",
                    noColorSpades = "AS TS KS 9S",
                    noColorClubs = "AC TC KC 9C"
                )
            ),
            Arguments.of(
                DeckMode.HEARTS,
                ExpectedCardOrdering(
                    coloredCards = "TH QC QS QH QD JC JS JH JD AH KH 9H",
                    noColorDiamond = "AD TD KD 9D",
                    noColorHeart = "",
                    noColorSpades = "AS TS KS 9S",
                    noColorClubs = "AC TC KC 9C"
                )
            ),
            Arguments.of(
                DeckMode.SPADES,
                ExpectedCardOrdering(
                    coloredCards = "TH QC QS QH QD JC JS JH JD AS TS KS 9S",
                    noColorDiamond = "AD TD KD 9D",
                    noColorHeart = "AH KH 9H",
                    noColorSpades = "",
                    noColorClubs = "AC TC KC 9C"
                )
            ),
            Arguments.of(
                DeckMode.CLUBS,
                ExpectedCardOrdering(
                    coloredCards = "TH QC QS QH QD JC JS JH JD AC TC KC 9C",
                    noColorDiamond = "AD TD KD 9D",
                    noColorHeart = "AH KH 9H",
                    noColorSpades = "AS TS KS 9S",
                    noColorClubs = ""
                )
            ),
            Arguments.of(
                DeckMode.QUEENS,
                ExpectedCardOrdering(
                    coloredCards = "QC QS QH QD",
                    noColorDiamond = "AD TD KD JD 9D",
                    noColorHeart = "AH TH KH JH 9H",
                    noColorSpades = "AS TS KS JS 9S",
                    noColorClubs = "AC TC KC JC 9C",
                )
            ),
            Arguments.of(
                DeckMode.JACKS,
                ExpectedCardOrdering(
                    coloredCards = "JC JS JH JD",
                    noColorDiamond = "AD TD KD QD 9D",
                    noColorHeart = "AH TH KH QH 9H",
                    noColorSpades = "AS TS KS QS 9S",
                    noColorClubs = "AC TC KC QC 9C"
                )
            ),
            Arguments.of(
                DeckMode.FREE,
                ExpectedCardOrdering(
                    coloredCards = "",
                    noColorDiamond = "AD TD KD QD JD 9D",
                    noColorHeart = "AH TH KH QH JH 9H",
                    noColorSpades = "AS TS KS QS JS 9S",
                    noColorClubs = "AC TC KC QC JC 9C"
                )
            )
        )
    }

    inner class ExpectedCardOrdering(
        val coloredCards: String,
        val noColorDiamond: String,
        val noColorHeart: String,
        val noColorSpades: String,
        val noColorClubs: String
    )
}