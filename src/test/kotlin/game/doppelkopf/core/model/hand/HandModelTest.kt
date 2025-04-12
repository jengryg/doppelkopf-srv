package game.doppelkopf.core.model.hand

import game.doppelkopf.core.cards.DeckMode
import game.doppelkopf.core.common.enums.Team
import game.doppelkopf.persistence.model.hand.HandEntity
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HandModelTest {
    @Nested
    inner class DetermineTeamByCards {
        @Test
        fun `hand has no queen of clubs`() {
            val hand = HandModel(
                HandEntity(
                    round = mockk { every { deckMode } returns DeckMode.DIAMONDS },
                    player = mockk(),
                    cardsRemaining = mutableListOf("TH0", "AS1", "JD0", "KC1", "AD0", "AS0"),
                    hasMarriage = false
                )
            )

            hand.determineTeamByCards()

            assertThat(hand.playerTeam).isEqualTo(Team.KO)
            assertThat(hand.internalTeam).isEqualTo(Team.KO)
            // public team must be hidden when determineTeamByCards is used
            assertThat(hand.publicTeam).isEqualTo(Team.NA)
        }

        @Test
        fun `hand has one queen of clubs`() {
            val hand = HandModel(
                HandEntity(
                    round = mockk { every { deckMode } returns DeckMode.DIAMONDS },
                    player = mockk(),
                    cardsRemaining = mutableListOf("TH0", "AS1", "JD0", "KC1", "AD0", "AS0", "QC0"),
                    hasMarriage = false
                )
            )

            hand.determineTeamByCards()

            assertThat(hand.playerTeam).isEqualTo(Team.RE)
            assertThat(hand.internalTeam).isEqualTo(Team.RE)
            // public team must be hidden when determineTeamByCards is used
            assertThat(hand.publicTeam).isEqualTo(Team.NA)
        }

        @Test
        fun `hand has both queen of clubs`() {
            val hand = HandModel(
                HandEntity(
                    round = mockk { every { deckMode } returns DeckMode.DIAMONDS },
                    player = mockk(),
                    cardsRemaining = mutableListOf("TH0", "AS1", "JD0", "KC1", "AD0", "AS0", "QC1", "QC0"),
                    hasMarriage = false
                )
            )

            hand.determineTeamByCards()

            assertThat(hand.playerTeam).isEqualTo(Team.RE)
            assertThat(hand.internalTeam).isEqualTo(Team.RE)
            // public team must be hidden when determineTeamByCards is used
            assertThat(hand.publicTeam).isEqualTo(Team.NA)
        }
    }
}