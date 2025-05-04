package game.doppelkopf.domain.round.service

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.core.errors.InvalidActionException
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.deck.enums.DeckMode
import game.doppelkopf.domain.hand.enums.Bidding
import game.doppelkopf.domain.hand.enums.Declaration
import game.doppelkopf.domain.hand.enums.Team
import game.doppelkopf.domain.round.enums.RoundContract
import game.doppelkopf.domain.round.enums.RoundState
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class RoundBidsEvaluationModelTest : BaseUnitTest() {
    @Test
    fun `round with marriage bidding only is configured as marriage round`() {
        val mfp = ModelFactoryProvider()

        val round = RoundBidsEvaluationModel(
            entity = createRoundWith(3, 1, 0),
            factoryProvider = mfp
        )

        val guard = round.canEvaluateBids()
        assertThat(guard.isSuccess).isTrue

        round.evaluateBids()

        assertThat(round.state).isEqualTo(RoundState.PLAYING_TRICKS)
        assertThat(round.deckMode).isEqualTo(DeckMode.DIAMONDS)
        assertThat(round.contract).isEqualTo(RoundContract.MARRIAGE_UNRESOLVED)

        round.hands.values.single { it.hasMarriage }.also {
            assertThat(it.internalTeam).isEqualTo(Team.RE)
            assertThat(it.playerTeam).isEqualTo(Team.RE)
            assertThat(it.publicTeam).isEqualTo(Team.RE)
        }

        round.hands.values.filterNot { it.hasMarriage }.forEach {
            // non married players are considered to be not in any team until the marriage is resolved
            assertThat(it.internalTeam).isEqualTo(Team.NA)
            assertThat(it.playerTeam).isEqualTo(Team.NA)
            assertThat(it.publicTeam).isEqualTo(Team.NA)
        }
    }

    @ParameterizedTest
    @EnumSource(RoundState::class, names = ["WAITING_FOR_BIDS"], mode = EnumSource.Mode.EXCLUDE)
    fun `guard yields exception when round is not in correct state`(roundState: RoundState) {
        val mfp = ModelFactoryProvider()

        val round = RoundBidsEvaluationModel(
            entity = createRoundWith(0, 0, 0).apply {
                state = roundState
            },
            factoryProvider = mfp
        )

        val guard = round.canEvaluateBids()
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Bids:Evaluate' can not be performed: The round must be in ${RoundState.WAITING_FOR_BIDS} state to process the bids.")

        assertThatThrownBy {
            round.evaluateBids()
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Bids:Evaluate' can not be performed: The round must be in ${RoundState.WAITING_FOR_BIDS} state to process the bids.")
    }

    @Test
    fun `guard yields exception when round has a hand that still needs to make a bid`() {
        val mfp = ModelFactoryProvider()

        val round = RoundBidsEvaluationModel(
            entity = createRoundWith(2, 1, 1),
            factoryProvider = mfp
        )

        val guard = round.canEvaluateBids()
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Bids:Evaluate' can not be performed: Not all players have finished their bids yet.")

        assertThatThrownBy {
            round.evaluateBids()
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Bids:Evaluate' can not be performed: Not all players have finished their bids yet.")
    }

    private fun createRoundWith(healthy: Int, marriage: Int, nothing: Int): RoundEntity {
        return RoundEntity(
            game = mockk(),
            dealer = mockk(),
            number = 1
        ).apply {
            state = RoundState.WAITING_FOR_BIDS

            repeat(healthy) {
                hands.add(
                    HandEntity(
                        round = this,
                        player = createPlayerEntity(seat = it),
                        index = it,
                        cardsRemaining = mutableListOf(),
                        hasMarriage = false
                    ).apply {
                        bidding = Bidding.NOTHING
                        declared = Declaration.HEALTHY
                    }
                )
            }

            repeat(marriage) {
                hands.add(
                    HandEntity(
                        round = this,
                        player = createPlayerEntity(seat = it + healthy),
                        index = it + healthy,
                        cardsRemaining = mutableListOf(),
                        hasMarriage = true
                    ).apply {
                        bidding = Bidding.MARRIAGE
                        declared = Declaration.RESERVATION
                    }
                )
            }

            repeat(nothing) {
                hands.add(
                    HandEntity(
                        round = this,
                        player = createPlayerEntity(seat = it + healthy + marriage),
                        index = it + healthy + marriage,
                        cardsRemaining = mutableListOf(),
                        hasMarriage = false
                    ).apply {
                        bidding = Bidding.NOTHING
                        declared = Declaration.RESERVATION
                    }
                )
            }
        }
    }
}