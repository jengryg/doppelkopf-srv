package game.doppelkopf.core.model.game.handler

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.core.cards.Deck
import game.doppelkopf.core.cards.DeckMode
import game.doppelkopf.core.common.enums.GameState
import game.doppelkopf.core.common.enums.Team
import game.doppelkopf.core.errors.ForbiddenActionException
import game.doppelkopf.core.errors.InvalidActionException
import game.doppelkopf.core.model.ModelFactoryProvider
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class GameDealModelTest : BaseUnitTest() {
    @Test
    fun `creates round and assigns it to the game, creates hands and assigns them to the round and players`() {
        val mfp = ModelFactoryProvider()

        val game = GameDealModel(
            entity = createGameEntity().apply {
                state = GameState.WAITING_FOR_DEAL
            },
            factoryProvider = mfp
        )

        val players = List(4) {
            createPlayerEntity(seat = it, game = game.entity).apply { dealer = false }
        }.let { pList -> mfp.player.create(pList) }.onEach { game.addPlayer(it) }

        val dealer = players[2].also {
            it.dealer = true
        }

        val guard = game.canDeal(user = dealer.user)
        assertThat(guard.isSuccess).isTrue

        val (round, hands) = game.deal(user = dealer.user)

        assertThat(round.game).isEqualTo(game)
        assertThat(round.dealer).isEqualTo(dealer)
        assertThat(round.number).isEqualTo(1)

        assertThat(game.rounds).hasSize(1)
        assertThat(game.rounds).containsKeys(1)
        assertThat(game.rounds[1]).isEqualTo(round)
        assertThat(game.state).isEqualTo(GameState.PLAYING_ROUND)

        hands.toList().also {
            val cards = it.flatMap { h -> h.entity.cardsRemaining }
            assertThat(cards).containsExactlyInAnyOrderElementsOf(
                // DeckMode does not matter, we just want to obtain all cards from the deck.
                Deck.create(DeckMode.DIAMONDS).cards.values.map { c -> c.encoded }
            )
        }.forEach {
            assertThat(it.round).isEqualTo(round)
            assertThat(it.entity.cardsRemaining).hasSize(12)
            val team = when (it.entity.cardsRemaining.any { c -> c == "QC0" || c == "QC1" }) {
                true -> Team.RE
                else -> Team.KO
            }
            assertThat(it.internalTeam).isEqualTo(team)
            assertThat(it.playerTeam).isEqualTo(team)
        }

        // Order should be 3,0,1,2 since 2 is the dealer, and we order by seat number ascending:
        assertThat(hands.first.player).isEqualTo(players[3])
        assertThat(hands.second.player).isEqualTo(players[0])
        assertThat(hands.third.player).isEqualTo(players[1])
        assertThat(hands.fourth.player).isEqualTo(players[2])
    }

    @ParameterizedTest
    @EnumSource(value = GameState::class, names = ["WAITING_FOR_DEAL"], mode = EnumSource.Mode.EXCLUDE)
    fun `guard yields exception when game is not in WAITING_FOR_DEAL state`(gameState: GameState) {
        val mfp = ModelFactoryProvider()

        val user = mfp.user.create(createUserEntity())

        val game = GameDealModel(
            entity = createGameEntity().apply {
                state = gameState
            },
            factoryProvider = mfp
        )

        val guard = game.canDeal(user = user)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Game:Deal' can not be performed: The game is currently not in the ${GameState.WAITING_FOR_DEAL} state.")

        assertThatThrownBy {
            game.deal(user = user)
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Game:Deal' can not be performed: The game is currently not in the ${GameState.WAITING_FOR_DEAL} state.")
    }

    @Test
    fun `guard yields exception when user is not the dealer`() {
        val mfp = ModelFactoryProvider()

        val game = GameDealModel(
            entity = createGameEntity().apply {
                state = GameState.WAITING_FOR_DEAL
                repeat(4) { players.add(createPlayerEntity(game = this, seat = it).apply { dealer = false }) }
            },
            factoryProvider = mfp
        )
        val player = game.players.values.toList().also {
            it[0].dealer = true
        }[1]

        val guard = game.canDeal(user = player.user)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(ForbiddenActionException::class.java)
            .hasMessageContaining("You are not allowed to perform the action 'Game:Deal': Only the current dealer of the game can deal this round.")

        assertThatThrownBy {
            game.deal(user = player.user)
        }.isInstanceOf(ForbiddenActionException::class.java)
            .hasMessageContaining("You are not allowed to perform the action 'Game:Deal': Only the current dealer of the game can deal this round.")
    }
}