package game.doppelkopf.core.handler.round

import game.doppelkopf.core.common.enums.Declaration
import game.doppelkopf.core.common.enums.RoundState
import game.doppelkopf.core.common.errors.InvalidActionException
import game.doppelkopf.core.model.round.RoundModel
import game.doppelkopf.persistence.model.round.RoundEntity
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RoundDeclarationEvaluationHandlerTest {
    @Nested
    inner class DoHandle {
        @ParameterizedTest
        @ValueSource(ints = [1, 2, 3, 4])
        fun `round with at least one reservation is advanced to auctioning phase`(numReservations: Int) {
            val round = RoundModel(
                createRoundWith(
                    healthy = 4 - numReservations,
                    silentMarriage = 0,
                    reservation = numReservations
                )
            )

            val handler = RoundDeclarationEvaluationHandler(round)

            val result = handler.doHandle()

            assertThat(result).isSameAs(round.entity)
            assertThat(result.state).isEqualTo(RoundState.WAITING_FOR_BIDS)
        }

        @ParameterizedTest
        @ValueSource(ints = [1, 2, 3])
        fun `round with silent marriage and at least one reservation is advanced to auctioning phase`(numReservations: Int) {
            val round = RoundModel(
                createRoundWith(
                    healthy = 3 - numReservations,
                    silentMarriage = 1,
                    reservation = numReservations
                )
            )


            val handler = RoundDeclarationEvaluationHandler(round)

            val result = handler.doHandle()

            assertThat(result).isSameAs(round.entity)
            assertThat(result.state).isEqualTo(RoundState.WAITING_FOR_BIDS)
        }

        @Test
        fun `detects normal round and delegates configuration`() {
            val round = spyk(RoundModel(createRoundWith(healthy = 4, silentMarriage = 0, reservation = 0)))
            every { round.configureAsNormalRound() } just Runs

            val handler = RoundDeclarationEvaluationHandler(round)

            val result = handler.doHandle()

            assertThat(result).isSameAs(round.entity)
            verify(exactly = 1) { round.configureAsNormalRound() }
        }

        @Test
        fun `detects silent marriage round and delegates configuration`() {
            val round = spyk(RoundModel(createRoundWith(healthy = 3, silentMarriage = 1, reservation = 0)))
            every { round.configureAsSilentMarriageRound() } just Runs

            val handler = RoundDeclarationEvaluationHandler(round)

            val result = handler.doHandle()

            assertThat(result).isSameAs(round.entity)
            verify(exactly = 1) { round.configureAsSilentMarriageRound() }
        }
    }

    @Nested
    inner class GuardBlockingCases {
        @ParameterizedTest
        @EnumSource(RoundState::class, names = ["WAITING_FOR_DECLARATIONS"], mode = EnumSource.Mode.EXCLUDE)
        fun `guard yields exception when round is not in correct state state`(roundState: RoundState) {
            val handler = RoundDeclarationEvaluationHandler(
                round = RoundModel(mockk { every { state } returns roundState })
            )

            val guard = handler.canHandle()
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Declaration:Process' can not be performed: The round must be in INITIALIZED state to process the declarations.")

            assertThatThrownBy {
                handler.doHandle()
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Declaration:Process' can not be performed: The round must be in INITIALIZED state to process the declarations.")
        }

        @Test
        fun `guard yields exception when round has a hand that still needs to make a declaration`() {
            val handler = RoundDeclarationEvaluationHandler(
                round = RoundModel(
                    mockk {
                        every { state } returns RoundState.WAITING_FOR_DECLARATIONS
                        every { hands } returns mutableSetOf(
                            mockk {
                                every { declared } returns Declaration.NOTHING
                            }
                        )
                    }
                )
            )

            val guard = handler.canHandle()
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Declaration:Process' can not be performed: Not all players have finished their declaration yet.")

            assertThatThrownBy {
                handler.doHandle()
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Declaration:Process' can not be performed: Not all players have finished their declaration yet.")
        }
    }

    private fun createRoundWith(healthy: Int, silentMarriage: Int, reservation: Int): RoundEntity {
        return RoundEntity(
            game = mockk(),
            dealer = mockk(),
            number = 1
        ).apply {
            state = RoundState.WAITING_FOR_DECLARATIONS

            repeat(healthy) {
                hands.add(mockk { every { declared } returns Declaration.HEALTHY })
            }
            repeat(silentMarriage) {
                hands.add(mockk { every { declared } returns Declaration.SILENT_MARRIAGE })
            }
            repeat(reservation) {
                hands.add(mockk { every { declared } returns Declaration.RESERVATION })
            }
        }
    }
}