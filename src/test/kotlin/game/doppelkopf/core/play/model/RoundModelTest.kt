package game.doppelkopf.core.play.model

import game.doppelkopf.core.cards.Deck
import game.doppelkopf.core.cards.DeckMode
import game.doppelkopf.persistence.game.GameEntity
import game.doppelkopf.persistence.game.PlayerEntity
import game.doppelkopf.persistence.play.RoundEntity
import game.doppelkopf.utils.Quadruple
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RoundModelTest {
    @Nested
    inner class CreatePlayerHands {
        @Test
        fun `createPlayerHands creates HandEntities and assigns them to this round for randomized deck`() {
            val game = mockk<GameEntity>()
            val players = Quadruple(
                PlayerEntity(user = mockk(), game = game, seat = 4),
                PlayerEntity(user = mockk(), game = game, seat = 6),
                PlayerEntity(user = mockk(), game = game, seat = 2),
                PlayerEntity(user = mockk(), game = game, seat = 1),
            )
            val round = RoundEntity(game = game, dealer = mockk(), number = 17)
            val deck = Deck.create(DeckMode.DIAMONDS)
            // The DeckMode is not important for the initial card distribution.

            val hands = RoundModel(round, deck).createPlayerHands(players)

            assertThat(hands.first.player).isEqualTo(players.first)
            assertThat(hands.second.player).isEqualTo(players.second)
            assertThat(hands.third.player).isEqualTo(players.third)
            assertThat(hands.fourth.player).isEqualTo(players.fourth)

            hands.toList().forEach {
                assertThat(it.cardsRemaining).hasSize(12)
                assertThat(it.round).isEqualTo(round)
            }

            assertThat(
                hands.toList().flatMap { it.cardsRemaining }
            ).containsExactlyInAnyOrderElementsOf(
                deck.cards.map { it.value.encoded }
            )
        }
    }
}