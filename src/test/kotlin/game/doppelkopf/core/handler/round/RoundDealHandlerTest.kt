package game.doppelkopf.core.handler.round

import game.doppelkopf.core.cards.Deck
import game.doppelkopf.core.cards.DeckMode
import game.doppelkopf.core.common.enums.GameState
import game.doppelkopf.core.common.errors.ForbiddenActionException
import game.doppelkopf.core.common.errors.InvalidActionException
import game.doppelkopf.core.model.game.GameModel
import game.doppelkopf.core.model.player.PlayerModel
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.persistence.model.game.GameEntity
import game.doppelkopf.persistence.model.player.PlayerEntity
import game.doppelkopf.persistence.model.user.UserEntity
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RoundDealHandlerTest {
    @Nested
    inner class DoHandle {
        @Test
        fun `creates round and assigns it to the game, creates hands and assigns them to the round`() {
            val user = UserModel(createTestUserEntity())
            val game = createTestGameEntity().apply {
                state = GameState.WAITING_FOR_DEAL
            }.let { GameModel(it) }
            val players = listOf(
                PlayerEntity(user = user.entity, game = game.entity, seat = 0).apply { dealer = true },
                PlayerEntity(user = createTestUserEntity(), game = game.entity, seat = 1).apply { dealer = false },
                PlayerEntity(user = createTestUserEntity(), game = game.entity, seat = 2).apply { dealer = false },
                PlayerEntity(user = createTestUserEntity(), game = game.entity, seat = 3).apply { dealer = false }
            ).map { PlayerModel(it) }.onEach {
                game.addPlayer(it)
            }

            val handler = RoundDealHandler(game = game, user = user)

            val (round, hands) = handler.doHandle()

            assertThat(game.entity.rounds).contains(round)
            assertThat(game.state).isEqualTo(GameState.PLAYING_ROUND)

            assertThat(round.game).isEqualTo(game.entity)
            assertThat(round.dealer.user).isEqualTo(user.entity)
            assertThat(round.number).isEqualTo(1)

            hands.toList().also {
                assertThat(it.flatMap { h -> h.cardsRemaining }).containsExactlyInAnyOrderElementsOf(
                    Deck.create(
                        DeckMode.DIAMONDS
                    ).cards.values.map { c -> c.encoded })
            }.forEach {
                assertThat(it.round).isEqualTo(round)
                assertThat(it.cardsRemaining).hasSize(12)
            }

            // Order should be 1,2,3,0 since 0 is the dealer, and we order by seat number ascending:
            assertThat(hands.first.player).isEqualTo(players[1].entity)
            assertThat(hands.second.player).isEqualTo(players[2].entity)
            assertThat(hands.third.player).isEqualTo(players[3].entity)
            assertThat(hands.fourth.player).isEqualTo(players[0].entity)
        }
    }

    @Nested
    inner class GuardBlockingCases {
        @ParameterizedTest
        @EnumSource(value = GameState::class, names = ["WAITING_FOR_DEAL"], mode = EnumSource.Mode.EXCLUDE)
        fun `guard yields exception when game is not in WAITING_FOR_DEAL state`(gameState: GameState) {
            val user = createTestUserEntity()
            val game = createTestGameEntity().apply {
                state = gameState
            }

            val handler = RoundDealHandler(
                game = GameModel(game),
                user = UserModel(user)
            )

            val guard = handler.canHandle()
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Round:Create' can not be performed: The game is currently not in the WAITING_FOR_DEAL state.")

            assertThatThrownBy {
                handler.doHandle()
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Round:Create' can not be performed: The game is currently not in the WAITING_FOR_DEAL state.")
        }

        @Test
        fun `guard yields exception when user is not the dealer`() {
            val user = createTestUserEntity()
            val game = createTestGameEntity().apply {
                state = GameState.WAITING_FOR_DEAL
                players.add(PlayerEntity(user = user, game = this, seat = 0).apply { dealer = false })
            }

            val handler = RoundDealHandler(
                game = GameModel(game),
                user = UserModel(user)
            )

            val guard = handler.canHandle()
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(ForbiddenActionException::class.java)
                .hasMessageContaining("You are not allowed to perform the action 'Round:Create': Only the current dealer of the game can deal this round.")

            assertThatThrownBy {
                handler.doHandle()
            }.isInstanceOf(ForbiddenActionException::class.java)
                .hasMessageContaining("You are not allowed to perform the action 'Round:Create': Only the current dealer of the game can deal this round.")
        }
    }

    private fun createTestGameEntity(): GameEntity {
        return GameEntity(
            creator = UserEntity(
                username = "username",
                password = "password"
            ),
            maxNumberOfPlayers = 4
        ).apply {
            state = GameState.INITIALIZED
        }
    }

    private fun createTestUserEntity(): UserEntity {
        return UserEntity(username = "username", password = "password")
    }
}