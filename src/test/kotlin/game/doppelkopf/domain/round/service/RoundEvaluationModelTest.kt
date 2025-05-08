package game.doppelkopf.domain.round.service

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.common.errors.InvalidActionException
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.deck.enums.DeckMode
import game.doppelkopf.domain.deck.model.Deck
import game.doppelkopf.domain.hand.enums.Team
import game.doppelkopf.domain.round.enums.RoundState
import game.doppelkopf.domain.trick.enums.TrickState
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class RoundEvaluationModelTest : BaseUnitTest() {
    @Test
    fun `evaluating round returns the KO and RE result`() {
        val cards = Deck.create(DeckMode.DIAMONDS).cards.values.toList()

        val round = createRoundEntity().apply {
            state = RoundState.PLAYING_TRICKS

            repeat(4) {
                hands.add(
                    createHandEntity(round = this, index = it, cards = mutableListOf(), hasMarriage = false).apply {
                        internalTeam = if (it % 2 == 0) Team.RE else Team.KO
                    }
                )
            }
        }

        repeat(cards.size / 4) { index ->
            val slice = cards.slice((index * 4)..(index * 4 + 3))

            round.tricks.add(
                createTrickEntity(
                    round = round,
                    number = index,
                    demand = slice.first().demand,
                    openIndex = index % 4
                ).apply {
                    state = TrickState.FOURTH_CARD_PLAYED
                    winner = round.hands.single { h -> h.index == index % 4 }
                    score = index * 5
                }
            )
        }

        val mfp = ModelFactoryProvider()

        val model = RoundEvaluationModel(
            entity = round,
            factoryProvider = mfp
        )

        val results = model.evaluateRound()

        results.re.also {
            assertThat(it.score).isEqualTo(150)
            assertThat(it.target).isEqualTo(121)

        }

        results.ko.also {
            assertThat(it.score).isEqualTo(180)
            assertThat(it.target).isEqualTo(120)
        }

        assertThat(round.state).isEqualTo(RoundState.EVALUATED)
    }

    @ParameterizedTest
    @EnumSource(RoundState::class, names = ["PLAYING_TRICKS"], mode = EnumSource.Mode.EXCLUDE)
    fun `guard yields exception when round is not in correct state`(roundState: RoundState) {
        val mfp = ModelFactoryProvider()

        val model = RoundEvaluationModel(
            entity = createRoundEntity().apply { state = roundState },
            factoryProvider = mfp
        )

        val guard = model.canEvaluateRound()
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: The round must be in PLAYING_TRICKS state to be evaluated.")

        assertThatThrownBy {
            model.evaluateRound()
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: The round must be in PLAYING_TRICKS state to be evaluated.")
    }

    @Test
    fun `guard yields exception when not all hands are empty`() {
        val round = createRoundEntity().apply {
            state = RoundState.PLAYING_TRICKS

            repeat(4) {
                hands.add(
                    createHandEntity(
                        index = it,
                        hasMarriage = false,
                        cards = MutableList(it) { "QC0" },
                        round = this
                    )
                )
            }
        }

        val mfp = ModelFactoryProvider()

        val model = RoundEvaluationModel(
            entity = round,
            factoryProvider = mfp
        )

        val guard = model.canEvaluateRound()
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: Not all hands of this round are empty.")

        assertThatThrownBy {
            model.evaluateRound()
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: Not all hands of this round are empty.")
    }

    @Test
    fun `guard yields exception when round has no current trick`() {
        val round = createRoundEntity().apply {
            state = RoundState.PLAYING_TRICKS
        }

        val mfp = ModelFactoryProvider()

        val model = RoundEvaluationModel(
            entity = round,
            factoryProvider = mfp
        )

        val guard = model.canEvaluateRound()
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: Could not determine the last trick of the round RoundEntity ")

        assertThatThrownBy {
            model.evaluateRound()
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: Could not determine the last trick of the round RoundEntity ")
    }

    @Test
    fun `guard yields exception when current trick has no winner yet`() {
        val round = createRoundEntity().apply {
            state = RoundState.PLAYING_TRICKS

            tricks.add(
                createTrickEntity(round = this).apply { winner = null }
            )
        }

        val mfp = ModelFactoryProvider()

        val model = RoundEvaluationModel(
            entity = round,
            factoryProvider = mfp
        )

        val guard = model.canEvaluateRound()
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: There is no winner in the current TrickEntity ")

        assertThatThrownBy {
            model.evaluateRound()
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: There is no winner in the current TrickEntity ")
    }
}