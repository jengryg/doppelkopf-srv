package game.doppelkopf.core.handler.game

import game.doppelkopf.core.common.enums.GameState
import game.doppelkopf.core.common.errors.ForbiddenActionException
import game.doppelkopf.core.common.errors.InvalidActionException
import game.doppelkopf.core.model.game.GameModel
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.persistence.model.game.GameEntity
import game.doppelkopf.persistence.model.player.PlayerEntity
import game.doppelkopf.persistence.model.user.UserEntity
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameStartHandlerTest {
    @Nested
    inner class DoHandle {
        @Test
        fun `call start on game model`() {
            val game = createTestGameEntity().apply {
                state = GameState.INITIALIZED

                repeat(4) {
                    players.add(PlayerEntity(user = mockk(), game = this, seat = it))
                }
            }.let { spyk(GameModel(it)) }

            val handler = GameStartHandler(game = game, user = game.creator)

            val result = handler.doHandle()

            assertThat(result).isEqualTo(game.entity)

            verify(exactly = 1) { game.start() }
        }
    }

    @Nested
    inner class GuardBlockingCases {
        @Test
        fun `guard yields exception when user is not the creator`() {
            val user = UserModel(createTestUserEntity())
            val game = GameModel(createTestGameEntity())

            val handler = GameStartHandler(game = game, user = user)

            val guard = handler.canHandle()
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(ForbiddenActionException::class.java)
                .hasMessageContaining("You are not allowed to perform the action 'Game:Start': Only the creator of the game can start it.")

            assertThatThrownBy {
                handler.doHandle()
            }.isInstanceOf(ForbiddenActionException::class.java)
                .hasMessageContaining("You are not allowed to perform the action 'Game:Start': Only the creator of the game can start it.")
        }

        @ParameterizedTest
        @EnumSource(GameState::class, names = ["INITIALIZED"], mode = EnumSource.Mode.EXCLUDE)
        fun `guard yields exception when game is not in INITIALIZED state`(gameState: GameState) {
            val game = GameModel(createTestGameEntity().apply { state = gameState })

            val handler = GameStartHandler(game = game, user = game.creator)

            val guard = handler.canHandle()
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Start' can not be performed: The game has been started already.")

            assertThatThrownBy {
                handler.doHandle()
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Start' can not be performed: The game has been started already.")
        }

        @ParameterizedTest
        @ValueSource(ints = [0, 1, 2, 3])
        fun `guard yields exception when game has less than 4 players`(playerCount: Int) {
            val game = createTestGameEntity().apply {
                repeat(playerCount) {
                    players.add(mockk())
                }
            }.let { GameModel(it) }
            val handler = GameStartHandler(game = game, user = game.creator)

            val guard = handler.canHandle()
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Start' can not be performed: The game needs to have at least 4 players.")

            assertThatThrownBy {
                handler.doHandle()
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Start' can not be performed: The game needs to have at least 4 players.")
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