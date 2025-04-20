package game.doppelkopf.core.model.round

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.core.cards.DeckMode
import game.doppelkopf.core.common.enums.*
import game.doppelkopf.core.common.errors.InvalidActionException
import game.doppelkopf.persistence.model.hand.HandEntity
import game.doppelkopf.persistence.model.round.RoundEntity
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

class RoundModelTest : BaseUnitTest() {
    @Nested
    inner class EvaluateDeclarations {
        @ParameterizedTest
        @ValueSource(ints = [1, 2, 3, 4])
        fun `round with at least one reservation is advanced to auctioning phase`(numReservations: Int) {
            val round = RoundModel.create(
                entity = createRoundWith(
                    healthy = 4 - numReservations,
                    silentMarriage = 0,
                    reservation = numReservations,
                    nothing = 0
                )
            )

            val guard = round.canEvaluateDeclarations()
            assertThat(guard.isSuccess).isTrue

            round.evaluateDeclarations()

            assertThat(round.state).isEqualTo(RoundState.WAITING_FOR_BIDS)
        }

        @ParameterizedTest
        @ValueSource(ints = [1, 2, 3])
        fun `round with silent marriage and at least one reservation is advanced to auctioning phase`(numReservations: Int) {
            val round = RoundModel.create(
                entity = createRoundWith(
                    healthy = 3 - numReservations,
                    silentMarriage = 1,
                    reservation = numReservations,
                    nothing = 0
                )
            )

            val guard = round.canEvaluateDeclarations()
            assertThat(guard.isSuccess).isTrue

            round.evaluateDeclarations()

            assertThat(round.state).isEqualTo(RoundState.WAITING_FOR_BIDS)
        }

        @Test
        fun `round with only healthy declarations is configured as normal round`() {
            val round = RoundModel.create(
                entity = createRoundWith(
                    healthy = 4,
                    silentMarriage = 0,
                    reservation = 0,
                    nothing = 0
                )
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
            val round = RoundModel.create(
                entity = createRoundWith(
                    healthy = 3,
                    silentMarriage = 1,
                    reservation = 0,
                    nothing = 0
                )
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
        fun `guard yields exception when round is not in correct state state`(roundState: RoundState) {
            val round = RoundModel.create(
                entity = createRoundWith(
                    healthy = 0,
                    silentMarriage = 0,
                    reservation = 0,
                    nothing = 0
                ).apply { state = roundState }
            )

            val guard = round.canEvaluateDeclarations()
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Declaration:Process' can not be performed: The round must be in ${RoundState.WAITING_FOR_DECLARATIONS} state to process the declarations.")

            assertThatThrownBy {
                round.evaluateDeclarations()
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Declaration:Process' can not be performed: The round must be in ${RoundState.WAITING_FOR_DECLARATIONS} state to process the declarations.")
        }

        @Test
        fun `guard yields exception when round has a hand that still needs to make a declaration`() {
            val round = RoundModel.create(
                entity = createRoundWith(
                    healthy = 3,
                    silentMarriage = 0,
                    reservation = 0,
                    nothing = 1
                )
            )

            val guard = round.canEvaluateDeclarations()
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Declaration:Process' can not be performed: Not all players have finished their declaration yet.")

            assertThatThrownBy {
                round.evaluateDeclarations()
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Declaration:Process' can not be performed: Not all players have finished their declaration yet.")
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

    @Nested
    inner class EvaluateBidding {
        @Test
        fun `round with marriage bidding only is configured as marriage round`() {
            val round = RoundModel.create(
                entity = createRoundWith(3, 1, 0)
            )

            val guard = round.canEvaluateBidding()
            assertThat(guard.isSuccess).isTrue

            round.evaluateBidding()

            assertThat(round.state).isEqualTo(RoundState.PLAYING_TRICKS)
            assertThat(round.deckMode).isEqualTo(DeckMode.DIAMONDS)
            assertThat(round.contract).isEqualTo(RoundContract.WEDDING)

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
            val round = RoundModel.create(
                entity = createRoundWith(0, 0, 0).apply {
                    state = roundState
                }
            )

            val guard = round.canEvaluateBidding()
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Bidding:Process' can not be performed: The round must be in ${RoundState.WAITING_FOR_BIDS} state to process the bids.")

            assertThatThrownBy {
                round.evaluateBidding()
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Bidding:Process' can not be performed: The round must be in ${RoundState.WAITING_FOR_BIDS} state to process the bids.")
        }

        @Test
        fun `guard yields exception when round has a hand that still needs to make a bid`() {
            val round = RoundModel.create(
                entity = createRoundWith(2, 1, 1)
            )

            val guard = round.canEvaluateBidding()
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Bidding:Process' can not be performed: Not all players have finished their bids yet.")

            assertThatThrownBy {
                round.evaluateBidding()
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Bidding:Process' can not be performed: Not all players have finished their bids yet.")
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

    @Nested
    inner class Create {
        @Test
        fun `create uses one model per entity`() {
            val entity = createRoundEntity()

            val model = RoundModel.create(entity)
            val other = RoundModel.create(entity)

            assertThat(model).isSameAs(other)
        }
    }
}