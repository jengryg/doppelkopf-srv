package game.doppelkopf.core.model.round

import game.doppelkopf.core.cards.DeckMode
import game.doppelkopf.core.common.enums.RoundContract
import game.doppelkopf.core.common.enums.RoundState
import game.doppelkopf.core.common.enums.Team
import game.doppelkopf.core.model.hand.HandModel
import game.doppelkopf.persistence.model.hand.HandEntity
import game.doppelkopf.persistence.model.round.RoundEntity
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RoundModelTest {
    @Nested
    inner class RoundConfigurations {
        @Test
        fun `configure normal round`() {
            val round = RoundModel(RoundEntity(game = mockk(), dealer = mockk(), number = 1))
            val hands = List(4) {
                HandEntity(
                    round = round.entity,
                    player = mockk(),
                    cardsRemaining = mockk(),
                    hasMarriage = false
                )
            }.map { spyk(HandModel(it)) }.onEach {
                round.addHand(it)
                every { it.determineTeamByCards() } just Runs
            }

            round.configureAsNormalRound()

            assertThat(round.state).isEqualTo(RoundState.PLAYING_TRICKS)
            assertThat(round.deckMode).isEqualTo(DeckMode.DIAMONDS)
            assertThat(round.contract).isEqualTo(RoundContract.NORMAL)

            hands.forEach {
                verify(exactly = 1) { it.determineTeamByCards() }
            }
        }

        @Test
        fun `configure silent marriage round`() {
            val round = RoundModel(RoundEntity(game = mockk(), dealer = mockk(), number = 1))
            val hands = List(4) {
                HandEntity(
                    round = round.entity,
                    player = mockk(),
                    cardsRemaining = mockk(),
                    hasMarriage = false
                )
            }.map { spyk(HandModel(it)) }.onEach {
                round.addHand(it)
                every { it.determineTeamByCards() } just Runs
            }

            round.configureAsNormalRound()

            assertThat(round.state).isEqualTo(RoundState.PLAYING_TRICKS)
            assertThat(round.deckMode).isEqualTo(DeckMode.DIAMONDS)
            assertThat(round.contract).isEqualTo(RoundContract.SILENT_MARRIAGE)

            hands.forEach {
                verify(exactly = 1) { it.determineTeamByCards() }
            }
        }

        @Test
        fun `configure marriage round`() {
            val round = RoundModel(RoundEntity(game = mockk(), dealer = mockk(), number = 1))
            val hands = List(4) {
                HandEntity(
                    round = round.entity,
                    player = mockk(),
                    cardsRemaining = mockk(),
                    hasMarriage = it == 0
                )
            }.map { spyk(HandModel(it)) }.onEach {
                round.addHand(it)
            }

            round.configureAsNormalRound()

            assertThat(round.state).isEqualTo(RoundState.PLAYING_TRICKS)
            assertThat(round.deckMode).isEqualTo(DeckMode.DIAMONDS)
            assertThat(round.contract).isEqualTo(RoundContract.WEDDING)

            hands.forEach {
                verify(exactly = 0) { it.determineTeamByCards() }
            }

            assertThat(hands[0].internalTeam).isEqualTo(Team.RE)
            assertThat(hands[0].playerTeam).isEqualTo(Team.RE)
            assertThat(hands[0].publicTeam).isEqualTo(Team.RE)

            hands.slice(1..3).forEach {
                assertThat(it.internalTeam).isEqualTo(Team.NA)
                assertThat(it.playerTeam).isEqualTo(Team.NA)
                assertThat(it.publicTeam).isEqualTo(Team.NA)
            }
        }
    }
}