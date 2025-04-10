package game.doppelkopf.core.play.processor

import game.doppelkopf.core.errors.InvalidActionException
import game.doppelkopf.core.play.enums.Bidding
import game.doppelkopf.core.play.enums.Declaration
import game.doppelkopf.core.play.enums.RoundState
import game.doppelkopf.persistence.play.RoundEntity
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BiddingProcessorTest {
    @Nested
    inner class ConstructorGuard {
        @ParameterizedTest
        @EnumSource(RoundState::class, names = ["DECLARED"], mode = EnumSource.Mode.EXCLUDE)
        fun `factory returns failure result when round is not in declared state`(roundState: RoundState) {
            val instance = BiddingProcessor.createWhenReady(
                mockk { every { state } returns roundState }
            )

            assertThat(instance.isFailure).isTrue()
            instance.onFailure {
                assertThat(it).isInstanceOf(InvalidActionException::class.java)
                    .hasMessageContaining("The action 'Bidding:Process' can not be performed: The round must be in declared state to process the bids.")
            }
        }

        @Test
        fun `factory returns failure result when round has a hand that still needs to make a bid`() {
            val instance = BiddingProcessor.createWhenReady(
                mockk {
                    every { state } returns RoundState.DECLARED
                    every { hands } returns mutableSetOf(
                        mockk {
                            every { declared } returns Declaration.RESERVATION
                            every { bidding } returns Bidding.NOTHING
                        }
                    )
                }
            )

            assertThat(instance.isFailure).isTrue()
            instance.onFailure {
                assertThat(it).isInstanceOf(InvalidActionException::class.java)
                    .hasMessageContaining("Not all players have finished their bids yet.")
            }
        }
    }

    @Nested
    inner class Processing {
        @Test
        fun `marriage round is processed correctly`() {
            mockkObject(RoundConfigurator)
            every { RoundConfigurator.configureMarriageRound(any()) } just Runs

            val round = createOnlyMarriageRound()
            val instance = BiddingProcessor.createWhenReady(round)

            assertThat(instance.isSuccess).isTrue()

            instance.onSuccess {
                it.process()
            }

            verify(exactly = 1) { RoundConfigurator.configureMarriageRound(round) }
            unmockkObject(RoundConfigurator)
        }

        private fun createOnlyMarriageRound(): RoundEntity {
            return RoundEntity(
                game = mockk(),
                dealer = mockk(),
                number = 1
            ).apply {
                state = RoundState.DECLARED

                hands.add(
                    mockk {
                        every { bidding } returns Bidding.NOTHING
                        every { declared } returns Declaration.HEALTHY
                    }
                )
                hands.add(
                    mockk {
                        every { bidding } returns Bidding.NOTHING
                        every { declared } returns Declaration.HEALTHY
                    }
                )
                hands.add(
                    mockk {
                        every { bidding } returns Bidding.WEDDING
                        every { declared } returns Declaration.RESERVATION
                    }
                )
                hands.add(
                    mockk {
                        every { bidding } returns Bidding.NOTHING
                        every { declared } returns Declaration.HEALTHY
                    }
                )
            }
        }
    }
}