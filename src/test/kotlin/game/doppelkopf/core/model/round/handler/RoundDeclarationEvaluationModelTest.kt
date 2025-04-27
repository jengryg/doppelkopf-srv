package game.doppelkopf.core.model.round.handler

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.core.cards.DeckMode
import game.doppelkopf.core.common.enums.Declaration
import game.doppelkopf.core.common.enums.RoundContract
import game.doppelkopf.core.common.enums.RoundState
import game.doppelkopf.core.common.errors.InvalidActionException
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.persistence.model.hand.HandEntity
import game.doppelkopf.persistence.model.round.RoundEntity
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

class RoundDeclarationEvaluationModelTest : BaseUnitTest() {
    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3, 4])
    fun `round with at least one reservation is advanced to auctioning phase`(numReservations: Int) {
        val mfp = ModelFactoryProvider()

        val round = RoundDeclarationEvaluationModel(
            entity = createRoundWith(
                healthy = 4 - numReservations,
                silentMarriage = 0,
                reservation = numReservations,
                nothing = 0
            ),
            factoryProvider = mfp
        )

        val guard = round.canEvaluateDeclarations()
        assertThat(guard.isSuccess).isTrue

        round.evaluateDeclarations()

        assertThat(round.state).isEqualTo(RoundState.WAITING_FOR_BIDS)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun `round with silent marriage and at least one reservation is advanced to auctioning phase`(numReservations: Int) {
        val mfp = ModelFactoryProvider()

        val round = RoundDeclarationEvaluationModel(
            entity = createRoundWith(
                healthy = 3 - numReservations,
                silentMarriage = 1,
                reservation = numReservations,
                nothing = 0
            ),
            factoryProvider = mfp
        )

        val guard = round.canEvaluateDeclarations()
        assertThat(guard.isSuccess).isTrue

        round.evaluateDeclarations()

        assertThat(round.state).isEqualTo(RoundState.WAITING_FOR_BIDS)
    }

    @Test
    fun `round with only healthy declarations is configured as normal round`() {
        val mfp = ModelFactoryProvider()

        val round = RoundDeclarationEvaluationModel(
            entity = createRoundWith(
                healthy = 4,
                silentMarriage = 0,
                reservation = 0,
                nothing = 0
            ),
            factoryProvider = mfp
        )

        val guard = round.canEvaluateDeclarations()
        assertThat(guard.isSuccess).isTrue

        round.evaluateDeclarations()

        assertThat(round.state).isEqualTo(RoundState.PLAYING_TRICKS)
        assertThat(round.deckMode).isEqualTo(DeckMode.DIAMONDS)
        assertThat(round.contract).isEqualTo(RoundContract.NORMAL)
    }

    @Test
    fun `round with silent marriage and no reservations is configured as silent marriage round`() {
        val mfp = ModelFactoryProvider()

        val round = RoundDeclarationEvaluationModel(
            entity = createRoundWith(
                healthy = 3,
                silentMarriage = 1,
                reservation = 0,
                nothing = 0
            ),
            factoryProvider = mfp
        )

        val guard = round.canEvaluateDeclarations()
        assertThat(guard.isSuccess).isTrue

        round.evaluateDeclarations()

        assertThat(round.state).isEqualTo(RoundState.PLAYING_TRICKS)
        assertThat(round.deckMode).isEqualTo(DeckMode.DIAMONDS)
        assertThat(round.contract).isEqualTo(RoundContract.SILENT_MARRIAGE)
    }

    @ParameterizedTest
    @EnumSource(RoundState::class, names = ["WAITING_FOR_DECLARATIONS"], mode = EnumSource.Mode.EXCLUDE)
    fun `guard yields exception when round is not in correct state`(roundState: RoundState) {
        val mfp = ModelFactoryProvider()

        val round = RoundDeclarationEvaluationModel(
            entity = createRoundWith(
                healthy = 0,
                silentMarriage = 0,
                reservation = 0,
                nothing = 0
            ).apply { state = roundState },
            factoryProvider = mfp
        )

        val guard = round.canEvaluateDeclarations()
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Declarations:Evaluate' can not be performed: The round must be in ${RoundState.WAITING_FOR_DECLARATIONS} state to process the declarations.")

        assertThatThrownBy {
            round.evaluateDeclarations()
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Declarations:Evaluate' can not be performed: The round must be in ${RoundState.WAITING_FOR_DECLARATIONS} state to process the declarations.")
    }

    @Test
    fun `guard yields exception when round has a hand that still needs to make a declaration`() {
        val mfp = ModelFactoryProvider()

        val round = RoundDeclarationEvaluationModel(
            entity = createRoundWith(
                healthy = 3,
                silentMarriage = 0,
                reservation = 0,
                nothing = 1
            ),
            factoryProvider = mfp
        )

        val guard = round.canEvaluateDeclarations()
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Declarations:Evaluate' can not be performed: Not all players have finished their declaration yet.")

        assertThatThrownBy {
            round.evaluateDeclarations()
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Declarations:Evaluate' can not be performed: Not all players have finished their declaration yet.")
    }

    private fun createRoundWith(healthy: Int, silentMarriage: Int, reservation: Int, nothing: Int): RoundEntity {
        return RoundEntity(
            game = mockk(),
            dealer = mockk(),
            number = 1
        ).apply {
            state = RoundState.WAITING_FOR_DECLARATIONS

            repeat(healthy) {
                hands.add(
                    HandEntity(
                        round = this,
                        player = createPlayerEntity(seat = it),
                        index = it,
                        cardsRemaining = mutableListOf(),
                        hasMarriage = false
                    ).apply {
                        declared = Declaration.HEALTHY
                    }
                )
            }
            repeat(silentMarriage) {
                hands.add(
                    HandEntity(
                        round = this,
                        player = createPlayerEntity(seat = it + healthy),
                        index = it + healthy,
                        cardsRemaining = mutableListOf(),
                        hasMarriage = false
                    ).apply {
                        declared = Declaration.SILENT_MARRIAGE
                    }
                )
            }
            repeat(reservation) {
                hands.add(
                    HandEntity(
                        round = this,
                        player = createPlayerEntity(seat = it + healthy + silentMarriage),
                        index = it + healthy + silentMarriage,
                        cardsRemaining = mutableListOf(),
                        hasMarriage = false
                    ).apply {
                        declared = Declaration.RESERVATION
                    }
                )
            }
            repeat(nothing) {
                hands.add(
                    HandEntity(
                        round = this,
                        player = createPlayerEntity(seat = it + healthy + silentMarriage + reservation),
                        index = it + healthy + silentMarriage + reservation,
                        cardsRemaining = mutableListOf(),
                        hasMarriage = false
                    ).apply {
                        declared = Declaration.NOTHING
                    }
                )
            }
        }
    }
}