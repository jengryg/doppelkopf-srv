package game.doppelkopf.core.handler.game

import game.doppelkopf.core.common.enums.GameState
import game.doppelkopf.core.common.errors.InvalidActionException
import game.doppelkopf.core.model.game.GameModel
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.persistence.model.game.GameEntity
import game.doppelkopf.persistence.model.player.PlayerEntity
import game.doppelkopf.persistence.model.user.UserEntity
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameJoinHandlerTest {
    @Nested
    inner class DoHandle {
        @Test
        fun `creates player and assigns it to the game`() {
            val user = UserModel(createTestUserEntity())
            val game = createTestGameEntity().apply {
                state = GameState.INITIALIZED
            }.let { GameModel(it) }

            val handler = GameJoinHandler(game = game, user = user)

            val result = handler.doHandle(seat = 7)

            assertThat(game.entity.players).contains(result)
            assertThat(result.game).isEqualTo(game.entity)
            assertThat(result.user).isEqualTo(user.entity)
            assertThat(result.seat).isEqualTo(7)
            assertThat(result.dealer).isFalse()
        }
    }

    @Nested
    inner class GuardBlockingCases {
        @ParameterizedTest
        @EnumSource(GameState::class, names = ["INITIALIZED"], mode = EnumSource.Mode.EXCLUDE)
        fun `guard yields exception when game is not in INITIALIZED state`(gameState: GameState) {
            val user = createTestUserEntity()
            val game = createTestGameEntity().apply {
                state = gameState
            }

            val handler = GameJoinHandler(
                game = GameModel(game),
                user = UserModel(user)
            )

            val guard = handler.canHandle(seat = 1)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Join' can not be performed: You can not join a game that has already started.")

            assertThatThrownBy {
                handler.doHandle(seat = 1)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Join' can not be performed: You can not join a game that has already started.")
        }

        @Test
        fun `guard yields exception when user already joined the game`() {
            val user = createTestUserEntity()
            val game = createTestGameEntity().apply {
                players.add(PlayerEntity(user = user, game = this, seat = 0))
            }

            val handler = GameJoinHandler(
                game = GameModel(game),
                user = UserModel(user)
            )

            val guard = handler.canHandle(1)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Join' can not be performed: You already joined this game.")

            assertThatThrownBy {
                handler.doHandle(1)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Join' can not be performed: You already joined this game.")
        }

        @Test
        fun `guard yields exception when seat is already taken`() {
            val user = createTestUserEntity()
            val game = createTestGameEntity().apply {
                players.add(PlayerEntity(user = createTestUserEntity(), game = this, seat = 0))
            }

            val handler = GameJoinHandler(
                game = GameModel(game),
                user = UserModel(user)
            )

            val guard = handler.canHandle(seat = 0)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Join' can not be performed: The seat you have chosen is already taken by another player.")

            assertThatThrownBy {
                handler.doHandle(seat = 0)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Join' can not be performed: The seat you have chosen is already taken by another player.")
        }

        @Test
        fun `guard yields exception when game is already at max player limit`() {
            val user = createTestUserEntity()
            val game = createTestGameEntity().apply {
                repeat(4) { players.add(mockk()) }
            }

            val handler = GameJoinHandler(
                game = GameModel(game),
                user = UserModel(user)
            )

            val guard = handler.canHandle(7)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Join' can not be performed: This game is already at its maximum capacity.")

            assertThatThrownBy {
                handler.doHandle(7)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Join' can not be performed: This game is already at its maximum capacity.")
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