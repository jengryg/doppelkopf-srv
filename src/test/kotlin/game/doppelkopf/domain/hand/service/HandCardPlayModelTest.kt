package game.doppelkopf.domain.hand.service

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.domain.deck.enums.CardDemand
import game.doppelkopf.common.errors.InvalidActionException
import game.doppelkopf.domain.ModelFactoryProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class HandCardPlayModelTest : BaseUnitTest() {
    @ParameterizedTest
    @MethodSource("provideFakeCardPlays")
    fun `yields exception when hand does not contain card`(
        cards: List<String>,
        cardToPlay: String
    ) {
        val mfp = ModelFactoryProvider()

        val model = HandCardPlayModel(
            entity = createHandEntity(
                cards = cards.toMutableList(),
            ),
            factoryProvider = mfp
        )
        // parse the given card representation with the deck of the round
        val card = model.round.deck.getCard(cardToPlay).getOrThrow()

        val result = model.playCard(card, card.demand)

        assertThat(result.isFailure).isTrue
        assertThat(result.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Play:Card' can not be performed: The hand does not contain the card $card.")

    }

    fun provideFakeCardPlays(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(listOf<String>(), "QC0"),
            Arguments.of(listOf<String>(), "QC1"),
            Arguments.of(listOf("QC0"), "QC1"),
            Arguments.of(listOf("TH0", "QC1"), "QC0"),
        )
    }

    @ParameterizedTest
    @MethodSource("provideIllegalCardPlays")
    fun `yields exception when card played it not legal`(
        cards: List<String>,
        cardToPlay: String,
        demand: CardDemand
    ) {
        val mfp = ModelFactoryProvider()

        val model = HandCardPlayModel(
            entity = createHandEntity(
                cards = cards.toMutableList(),
            ),
            factoryProvider = mfp
        )
        // parse the given card representation with the deck of the round
        val card = model.round.deck.getCard(cardToPlay).getOrThrow()

        val result = model.playCard(card, demand)

        assertThat(result.isFailure).isTrue
        assertThat(result.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Play:Card' can not be performed: You are not allowed to play $card into a trick that demands $demand when your hand can serve the demand.")
    }

    fun provideIllegalCardPlays(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(listOf("QC0", "AS0"), "QC0", CardDemand.SPADES),
            Arguments.of(listOf("QC0", "AS0"), "AS0", CardDemand.COLORED),
            Arguments.of(listOf("QC0", "AS0", "AH0"), "AS0", CardDemand.HEARTS),
        )
    }

    @ParameterizedTest
    @MethodSource("provideLegalCardPlays")
    fun `yields success when card is in hand and a legal move`(
        cards: List<String>,
        cardToPlay: String,
        demand: CardDemand
    ) {
        val mfp = ModelFactoryProvider()

        val model = HandCardPlayModel(
            entity = createHandEntity(
                cards = cards.toMutableList(),
            ),
            factoryProvider = mfp
        )
        // parse the given card representation with the deck of the round
        val card = model.round.deck.getCard(cardToPlay).getOrThrow()

        val result = model.playCard(card, demand)

        assertThat(result.isSuccess).isTrue
    }

    fun provideLegalCardPlays(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(listOf("AS0", "QC0"), "AS0", CardDemand.SPADES),
            Arguments.of(listOf("QC0", "AS0"), "QC0", CardDemand.HEARTS),
            Arguments.of(listOf("9H0", "AS0"), "AS0", CardDemand.COLORED),
            Arguments.of(listOf("AS0", "AH0"), "AS0", CardDemand.CLUBS),
        )
    }
}