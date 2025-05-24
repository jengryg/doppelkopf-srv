package game.doppelkopf.domain.game.service

import game.doppelkopf.BaseUnitTest
import game.doppelkopf.common.errors.ForbiddenActionException
import game.doppelkopf.common.errors.InvalidActionException
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.game.enums.GameState
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

class GameStartModelTest : BaseUnitTest() {
    @Test
    fun `start the game sets dealer and advances state`() {
        val mfp = ModelFactoryProvider()

        val game = GameStartModel(
            entity = createGameEntity().apply {
                repeat(4) { players.add(createPlayerEntity(seat = it, game = this)) }
            },
            factoryProvider = mfp
        )

        assertThatCode {
            game.start(game.creator)
        }.doesNotThrowAnyException()

        assertThat(game.state).isEqualTo(GameState.WAITING_FOR_DEAL)
        assertThat(game.started).isNotNull
        assertThat(game.players.values.count { !it.dealer }).isEqualTo(3)
        assertThat(game.players.values.count { it.dealer }).isEqualTo(1)
    }

    @Test
    fun `guard yields exception when user is not the creator`() {
        val mfp = ModelFactoryProvider()

        val notCreator = mfp.user.create(
            entity = createUserEntity()
        )
        val game = GameStartModel(entity = createGameEntity(), factoryProvider = mfp)

        val guard = game.canStart(notCreator)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(ForbiddenActionException::class.java)
            .hasMessageContaining("You are not allowed to perform this action: Only the creator of the game can start it.")

        assertThatThrownBy {
            game.start(notCreator)
        }.isInstanceOf(ForbiddenActionException::class.java)
            .hasMessageContaining("You are not allowed to perform this action: Only the creator of the game can start it.")
    }

    @ParameterizedTest
    @EnumSource(GameState::class, names = ["INITIALIZED"], mode = EnumSource.Mode.EXCLUDE)
    fun `guard yields exception when game is not in INITIALIZED state`(gameState: GameState) {
        val mfp = ModelFactoryProvider()

        val game = GameStartModel(
            entity = createGameEntity().apply { state = gameState },
            factoryProvider = mfp
        )

        val guard = game.canStart(game.creator)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: The game has been started already.")

        assertThatThrownBy {
            game.start(game.creator)
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: The game has been started already.")
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3])
    fun `guard yields exception when game has less than 4 players`(playerCount: Int) {
        val mfp = ModelFactoryProvider()

        val game = GameStartModel(
            entity = createGameEntity().apply {
                repeat(playerCount) {
                    players.add(createPlayerEntity(seat = it, game = this))
                }
            },
            factoryProvider = mfp
        )

        val guard = game.canStart(game.creator)
        assertThat(guard.isFailure).isTrue

        assertThat(guard.exceptionOrNull())
            .isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: The game needs to have at least 4 players.")

        assertThatThrownBy {
            game.start(game.creator)
        }.isInstanceOf(InvalidActionException::class.java)
            .hasMessageContaining("This action can not be performed: The game needs to have at least 4 players.")
    }
}