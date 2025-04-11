package game.doppelkopf.core.play.processor

import game.doppelkopf.core.common.errors.InvalidActionException
import game.doppelkopf.core.common.enums.Declaration
import game.doppelkopf.core.common.enums.RoundState
import game.doppelkopf.persistence.model.round.RoundEntity
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DeclarationProcessorTest {
    @Nested
    inner class ConstructorGuard {
        @ParameterizedTest
        @EnumSource(RoundState::class, names = ["INITIALIZED"], mode = EnumSource.Mode.EXCLUDE)
        fun `factory returns failure result when round is not in initialized state`(roundState: RoundState) {
            val instance = DeclarationProcessor.createWhenReady(
                mockk { every { state } returns roundState }
            )

            assertThat(instance.isFailure).isTrue()
            instance.onFailure {
                assertThat(it).isInstanceOf(InvalidActionException::class.java)
                    .hasMessageContaining("The action 'Declaration:Process' can not be performed: The round must be in INITIALIZED state to process the declarations.")
            }
        }

        @Test
        fun `factory returns failure result when round has a hand that still needs to make a declaration`() {
            val instance = DeclarationProcessor.createWhenReady(
                mockk {
                    every { state } returns RoundState.INITIALIZED
                    every { hands } returns mutableSetOf(
                        mockk {
                            every { declared } returns Declaration.NOTHING
                        }
                    )
                }
            )

            assertThat(instance.isFailure).isTrue()
            instance.onFailure {
                assertThat(it).isInstanceOf(InvalidActionException::class.java)
                    .hasMessageContaining("The action 'Declaration:Process' can not be performed: Not all players have finished their declaration yet.")
            }
        }
    }

    @Nested
    inner class Processing {
        @ParameterizedTest
        @ValueSource(ints = [1, 2, 3, 4])
        fun `reservation round is advanced to auctioning phase`(numReservations: Int) {
            // round with numReservations hands that declared RESERVATION and 4 - numReservations declared HEALTHY
            val round =
                createRoundWith(healthy = 4 - numReservations, silentMarriage = 0, reservation = numReservations)
            val instance = DeclarationProcessor.createWhenReady(round)

            assertThat(instance.isSuccess).isTrue()

            instance.onSuccess {
                it.process()
            }

            assertThat(round.state).isEqualTo(RoundState.DECLARED)
        }

        @ParameterizedTest
        @ValueSource(ints = [1, 2, 3])
        fun `silent marriage round with reservation is advanced to auctioning phase`(numReservations: Int) {
            // Whenever there is one reservation, we need to go into auctioning phase!
            val round =
                createRoundWith(healthy = 3 - numReservations, silentMarriage = 1, reservation = numReservations)
            val instance = DeclarationProcessor.createWhenReady(round)

            assertThat(instance.isSuccess).isTrue()

            instance.onSuccess {
                it.process()
            }

            assertThat(round.state).isEqualTo(RoundState.DECLARED)
        }

        @Test
        fun `normal round is processed correctly`() {
            mockkObject(RoundConfigurator)
            every { RoundConfigurator.configureNormalRound(any()) } just Runs

            val round = createRoundWith(healthy = 4, silentMarriage = 0, reservation = 0)
            val instance = DeclarationProcessor.createWhenReady(round)

            assertThat(instance.isSuccess).isTrue()

            instance.onSuccess {
                it.process()
            }

            verify(exactly = 1) { RoundConfigurator.configureNormalRound(round) }
        }

        @Test
        fun `silent marriage round is processed correctly`() {
            mockkObject(RoundConfigurator)
            every { RoundConfigurator.configureSilentMarriageRound(any()) } just Runs

            val round = createRoundWith(healthy = 3, silentMarriage = 1, reservation = 0)
            val instance = DeclarationProcessor.createWhenReady(round)

            assertThat(instance.isSuccess).isTrue()

            instance.onSuccess {
                it.process()
            }

            verify(exactly = 1) { RoundConfigurator.configureSilentMarriageRound(round) }
        }

        private fun createRoundWith(healthy: Int, silentMarriage: Int, reservation: Int): RoundEntity {
            return RoundEntity(
                game = mockk(),
                dealer = mockk(),
                number = 1
            ).apply {
                state = RoundState.INITIALIZED

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
}