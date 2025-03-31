package game.doppelkopf.core.game.model

import game.doppelkopf.core.errors.ForbiddenActionException
import game.doppelkopf.core.errors.InvalidActionException
import game.doppelkopf.core.game.enums.GameState
import game.doppelkopf.persistence.game.GameEntity
import game.doppelkopf.persistence.game.PlayerEntity
import game.doppelkopf.persistence.user.UserEntity
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameModelTest {

    @Nested
    inner class StartMethod {
        // TODO happy case

        @Test
        fun `start throws exception when user is not the creator`() {
            val game = createTestGameEntity()

            assertThatThrownBy {
                GameModel(game).start(UserEntity(username = "username", password = "password"))
            }.isInstanceOf(ForbiddenActionException::class.java)
                .hasMessageContaining("You are not allowed to perform the action 'Game:Start': Only the creator of the game can start it.")

        }

        @ParameterizedTest
        @EnumSource(GameState::class, names = ["INITIALIZED"], mode = EnumSource.Mode.EXCLUDE)
        fun `start throws exception when game is not in correct state`(gameState: GameState) {
            val game = createTestGameEntity().apply {
                state = gameState
            }

            assertThatThrownBy {
                GameModel(game).start(game.creator)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Start' can not be performed: The game has been started already.")
        }

        @ParameterizedTest
        @ValueSource(ints = [0, 1, 2, 3])
        fun `start throws exception when game has less than 4 players`(playerCount: Int) {
            val game = createTestGameEntity().apply {
                repeat(playerCount) {
                    players.add(mockk())
                }
            }

            assertThatThrownBy {
                GameModel(game).start(game.creator)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Start' can not be performed: The game needs to have at least 4 players.")
        }
    }

    @Nested
    inner class JoinMethod {
        // TODO happy case

        @ParameterizedTest
        @EnumSource(GameState::class, names = ["INITIALIZED"], mode = EnumSource.Mode.EXCLUDE)
        fun `join throws exception when game is not in correct state`(gameState: GameState) {
            val game = createTestGameEntity().apply {
                state = gameState
            }

            assertThatThrownBy {
                GameModel(game).join(UserEntity(username = "username", password = "password"), 1)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Join' can not be performed: You can not join a game that has already started.")
        }

        @Test
        fun `join throws exception when user already joined game`() {
            val user = UserEntity(username = "username", password = "password")

            val game = createTestGameEntity().apply {
                players.add(PlayerEntity(user = user, game = this, seat = 0))
            }

            assertThatThrownBy {
                GameModel(game).join(user, 1)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Join' can not be performed: You already joined this game.")
        }

        @Test
        fun `join throws exception when the user tries to use a already taken seat`() {
            val game = createTestGameEntity().apply {
                players.add(PlayerEntity(user = mockk(), game = this, seat = 0))
            }

            assertThatThrownBy {
                GameModel(game).join(UserEntity(username = "username", password = "password"), 0)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Join' can not be performed: The seat you have chosen is already taken by another player.")
        }

        @Test
        fun `join throws exception when game is already at max player limit`() {
            val game = createTestGameEntity().apply {
                repeat(4) { players.add(mockk()) }
            }

            assertThatThrownBy {
                GameModel(game).join(UserEntity(username = "username", password = "password"), 7)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Join' can not be performed: This game is already at its maximum capacity.")
        }
    }

    @Nested
    inner class NextRoundMethod {
        // TODO happy case

        @ParameterizedTest
        @EnumSource(value = GameState::class, names = ["WAITING_FOR_DEAL"], mode = EnumSource.Mode.EXCLUDE)
        fun `next round throws exception when game is not in correct state`(gameState: GameState) {
            val game = createTestGameEntity().apply {
                state = gameState
            }

            assertThatThrownBy {
                GameModel(game).nextRound(UserEntity(username = "username", password = "password"))
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Round:Create' can not be performed: The game is currently not in the WAITING_FOR_DEAL state.")
        }

        @Test
        fun `next round throws exception when user is not the dealer of the new round`() {
            val user = UserEntity(username = "username", password = "password")

            val game = createTestGameEntity().apply {
                state = GameState.WAITING_FOR_DEAL
                players.add(PlayerEntity(user = user, game = this, seat = 0).apply { dealer = false })
            }

            assertThatThrownBy {
                GameModel(game).nextRound(UserEntity(username = "username", password = "password"))
            }.isInstanceOf(ForbiddenActionException::class.java)
                .hasMessageContaining("You are not allowed to perform the action 'Round:Create': Only the current dealer of the game can start this round.")
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
}