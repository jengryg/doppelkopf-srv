package game.doppelkopf.domain.trick.service

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.core.common.enums.TrickState
import game.doppelkopf.core.errors.InvalidActionException
import game.doppelkopf.domain.ModelFactoryProvider
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class TrickEvaluationModelTest : BaseUnitTest() {
    @Test
    fun `evaluation of finished tricks succeeds`() {
        val round = createRoundEntity().apply {
            repeat(4) {
                hands.add(createHandEntity(index = it))
            }
        }
        val entity = createTrickEntity(round = round, openIndex = 2).apply {
            state = TrickState.FOURTH_CARD_PLAYED

            cards.add("AS0")
            cards.add("TS0")
            cards.add("QC0")
            cards.add("AD0")
        }

        val mfp = ModelFactoryProvider()

        val model = TrickEvaluationModel(entity, mfp)

        assertThatCode {
            model.evaluateTrick()
        }.doesNotThrowAnyException()

        assertThat(model.winner?.id).isEqualTo(round.hands.single { it.index == 0 }.id)
        assertThat(model.score).isEqualTo(35)
        assertThat(model.leadingCardIndex).isEqualTo(2)

    }

    @ParameterizedTest
    @EnumSource(TrickState::class, names = ["FOURTH_CARD_PLAYED"], mode = EnumSource.Mode.EXCLUDE)
    fun `guard yields exception when trick not in correct state`(trickState: TrickState) {
        val entity = createTrickEntity().apply { state = trickState }

        val mfp = ModelFactoryProvider()

        val model = TrickEvaluationModel(entity, mfp)

        val guard = model.canEvaluateTrick()
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Trick:Evaluate' can not be performed: The trick must be in ${TrickState.FOURTH_CARD_PLAYED} state to be evaluated.")

        assertThatThrownBy {
            model.evaluateTrick()
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Trick:Evaluate' can not be performed: The trick must be in ${TrickState.FOURTH_CARD_PLAYED} state to be evaluated.")
    }

    @Test
    fun `guard yields exception when trick already has a winner`() {
        val entity = createTrickEntity().apply {
            state = TrickState.FOURTH_CARD_PLAYED
            winner = createHandEntity()
        }

        val mfp = ModelFactoryProvider()

        val model = TrickEvaluationModel(entity, mfp)

        val guard = model.canEvaluateTrick()
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Trick:Evaluate' can not be performed: The trick already has a winner determined.")

        assertThatThrownBy {
            model.evaluateTrick()
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Trick:Evaluate' can not be performed: The trick already has a winner determined.")
    }
}