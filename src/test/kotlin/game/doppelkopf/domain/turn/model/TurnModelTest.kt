package game.doppelkopf.domain.turn.model

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.common.errors.GameFailedException
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.deck.enums.CardDemand
import game.doppelkopf.domain.deck.enums.DeckMode
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

class TurnModelTest : BaseUnitTest() {
    @Test
    fun `instantiate with entity from helper method`() {
        val mfp = ModelFactoryProvider()
        val entity = createTurnEntity()

        assertThatCode {
            mfp.turn.create(entity)
            TurnModel(entity, mfp)
        }.doesNotThrowAnyException()
    }

    @Test
    fun `decodes card based on rounds deck`() {
        val mfp = ModelFactoryProvider()
        val entity = createTurnEntity(card = "QC0", round = createRoundEntity().apply { deckMode = DeckMode.FREE })

        val model = mfp.turn.create(entity)

        assertThat(model.card.isQueenOfClubs).isTrue
        // deck mode is free, so QC0 is not colored
        assertThat(model.card.isColored).isFalse
        assertThat(model.card.demand).isEqualTo(CardDemand.CLUBS)
    }

    @Test
    fun `throws GameFailedException when card can not be decoded`() {
        val mfp = ModelFactoryProvider()
        val entity = createTurnEntity(card = "QC2")

        val model = mfp.turn.create(entity)

        assertThatThrownBy {
            model.card
        }.isInstanceOf(GameFailedException::class.java)
            .hasMessageContaining("Can not decode card of the turn")
    }
}