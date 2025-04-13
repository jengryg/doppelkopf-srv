package game.doppelkopf.core.handler.round

import game.doppelkopf.core.common.enums.Bidding
import game.doppelkopf.core.common.enums.Declaration
import game.doppelkopf.core.common.enums.RoundState
import game.doppelkopf.core.common.errors.InvalidActionException
import game.doppelkopf.core.model.round.RoundModel
import game.doppelkopf.core.play.processor.RoundConfigurator
import game.doppelkopf.persistence.model.round.RoundEntity
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RoundBiddingEvaluationHandlerTest {
    @Nested
    inner class DoHandle {
        @Test
        fun `detects marriage round and delegates configuration`() {
            // TODO: refactor round configurator
            mockkObject(RoundConfigurator)
            every { RoundConfigurator.configureMarriageRound(any()) } just Runs

            val round = createOnlyMarriageRound()
            val handler = RoundBiddingEvaluationHandler(RoundModel(round))

            val result = handler.doHandle()

            assertThat(result).isEqualTo(round)

            verify(exactly = 1) { RoundConfigurator.configureMarriageRound(round) }
        }
    }

    @Nested
    inner class GuardBlockingCases {
        @Nested
        inner class GuardBlockingCases {
            @ParameterizedTest
            @EnumSource(RoundState::class, names = ["DECLARED"], mode = EnumSource.Mode.EXCLUDE)
            fun `guard yields exception when round is not in DECLARED state`(roundState: RoundState) {
                val handler = RoundBiddingEvaluationHandler(
                    round = RoundModel(mockk { every { state } returns roundState })
                )

                val guard = handler.canHandle()
                assertThat(guard.isFailure).isTrue

                assertThat(guard.exceptionOrNull())
                    .isInstanceOf(InvalidActionException::class.java)
                    .hasMessageContaining("The action 'Bidding:Process' can not be performed: The round must be in declared state to process the bids.")

                assertThatThrownBy {
                    handler.doHandle()
                }.isInstanceOf(InvalidActionException::class.java)
                    .hasMessageContaining("The action 'Bidding:Process' can not be performed: The round must be in declared state to process the bids.")
            }

            @Test
            fun `guard yields exception when round has a hand that still needs to make a bid`() {
                val handler = RoundBiddingEvaluationHandler(
                    round = RoundModel(
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
                )

                val guard = handler.canHandle()
                assertThat(guard.isFailure).isTrue

                assertThat(guard.exceptionOrNull())
                    .isInstanceOf(InvalidActionException::class.java)
                    .hasMessageContaining("Not all players have finished their bids yet.")

                assertThatThrownBy {
                    handler.doHandle()
                }.isInstanceOf(InvalidActionException::class.java)
                    .hasMessageContaining("Not all players have finished their bids yet.")
            }
        }
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