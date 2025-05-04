package game.doppelkopf.domain.trick.handler

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.core.common.enums.TrickState
import game.doppelkopf.core.model.ModelFactoryProvider
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class TrickCardPlayModelTest : BaseUnitTest() {
    @ParameterizedTest
    @MethodSource("provideTrickCardsAndPlayCard")
    fun `add card to trick and update the trick`(
        trickCards: List<String>,
        cardToPlay: String,
        expectedScore: Int,
        expectedLeading: Int,
        expectedState: TrickState
    ) {
        val mfp = ModelFactoryProvider()

        val model = TrickCardPlayModel(
            entity = createTrickEntity().apply {
                cards.addAll(trickCards)
            },
            factoryProvider = mfp
        )
        // parse the given card representation with the deck of the round
        val card = model.round.deck.getCard(cardToPlay).getOrThrow()

        assertThatCode { model.playCard(card) }.doesNotThrowAnyException()

        assertThat(model.cards.map { it.encoded }).containsExactlyElementsOf(trickCards.plus(cardToPlay))
        assertThat(model.score).isEqualTo(expectedScore)
        assertThat(model.leadingCardIndex).isEqualTo(expectedLeading)
        assertThat(model.state).isEqualTo(expectedState)
    }

    fun provideTrickCardsAndPlayCard(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(listOf<String>(), "TH0", 10, 0, TrickState.FIRST_CARD_PLAYED),
            Arguments.of(listOf<String>(), "TH1", 10, 0, TrickState.FIRST_CARD_PLAYED),
            Arguments.of(listOf("AS0"), "AS1", 22, 0, TrickState.SECOND_CARD_PLAYED),
            Arguments.of(listOf("AS0"), "QC0", 14, 1, TrickState.SECOND_CARD_PLAYED),
            Arguments.of(listOf("AS0", "TS0"), "QC0", 24, 2, TrickState.THIRD_CARD_PLAYED),
            Arguments.of(listOf("AS0", "TS0", "TS1"), "QC0", 34, 3, TrickState.FOURTH_CARD_PLAYED),
            Arguments.of(listOf("AS0", "TS0", "TS1"), "AH0", 42, 0, TrickState.FOURTH_CARD_PLAYED)
        )
    }
}