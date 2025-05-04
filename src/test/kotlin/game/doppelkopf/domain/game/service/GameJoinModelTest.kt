package game.doppelkopf.domain.game.service

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.domain.game.enums.GameState
import game.doppelkopf.core.errors.InvalidActionException
import game.doppelkopf.domain.ModelFactoryProvider
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class GameJoinModelTest : BaseUnitTest() {
    @Test
    fun `creates player and assigns it to the game`() {
        val mfp = ModelFactoryProvider()
        val game = GameJoinModel(
            entity = createGameEntity().apply {
                state = GameState.INITIALIZED
            },
            factoryProvider = mfp
        )

        repeat(4) {
            // Game allows for 4 players, so we join the game with 4 unique users at 4 different seats for this test.
            val user = mfp.user.create(createUserEntity())

            val result = game.join(
                user = user,
                seat = it
            )

            assertThat(game.players).containsKey(user)
            assertThat(result.seat).isEqualTo(it)
            assertThat(result.user).isEqualTo(user)
            assertThat(result.dealer).isFalse()
            assertThat(result.game).isEqualTo(game)
        }
    }

    @ParameterizedTest
    @EnumSource(GameState::class, names = ["INITIALIZED"], mode = EnumSource.Mode.EXCLUDE)
    fun `guard yields exception when game is not in INITIALIZED state`(gameState: GameState) {
        val mfp = ModelFactoryProvider()

        val user = mfp.user.create(createUserEntity())
        val game = GameJoinModel(
            entity = createGameEntity().apply { state = gameState },
            factoryProvider = mfp
        )

        val guard = game.canJoin(user = user, seat = 1)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Game:Join' can not be performed: You can not join a game that has already started.")

        assertThatThrownBy {
            game.join(user = user, seat = 1)
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Game:Join' can not be performed: You can not join a game that has already started.")
    }

    @Test
    fun `guard yields exception when user already joined the game`() {
        val mfp = ModelFactoryProvider()

        val user = mfp.user.create(createUserEntity())

        val game = GameJoinModel(
            entity = createGameEntity().apply {
                players.add(createPlayerEntity(user = user.entity, game = this, seat = 0))
            },
            factoryProvider = mfp
        )

        val guard = game.canJoin(user = user, seat = 1)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Game:Join' can not be performed: You already joined this game.")

        assertThatThrownBy {
            game.join(user = user, seat = 1)
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Game:Join' can not be performed: You already joined this game.")
    }

    @Test
    fun `guard yields exception when seat is already taken`() {
        val mfp = ModelFactoryProvider()

        val user = mfp.user.create(createUserEntity())

        val game = GameJoinModel(
            entity = createGameEntity().apply {
                players.add(createPlayerEntity(user = createUserEntity(), game = this, seat = 0))
            },
            factoryProvider = mfp
        )

        val guard = game.canJoin(user = user, seat = 0)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Game:Join' can not be performed: The seat you have chosen is already taken by another player.")

        assertThatThrownBy {
            game.join(user = user, seat = 0)
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Game:Join' can not be performed: The seat you have chosen is already taken by another player.")
    }

    @Test
    fun `guard yields exception when game is already at max player limit`() {
        val mfp = ModelFactoryProvider()

        val user = mfp.user.create(createUserEntity())

        val game = GameJoinModel(
            entity = createGameEntity(maxNumberOfPlayers = 4).apply {
                repeat(4) {
                    players.add(createPlayerEntity(game = this, seat = it))
                }
            },
            factoryProvider = mfp
        )

        val guard = game.canJoin(user = user, seat = 7)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Game:Join' can not be performed: This game is already at its maximum capacity.")

        assertThatThrownBy {
            game.join(user = user, seat = 7)
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("The action 'Game:Join' can not be performed: This game is already at its maximum capacity.")
    }
}