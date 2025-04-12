package game.doppelkopf.core.model.game

import game.doppelkopf.core.common.enums.GameState
import game.doppelkopf.core.common.errors.GameFailedException
import game.doppelkopf.core.model.player.PlayerModel
import game.doppelkopf.persistence.model.game.GameEntity
import game.doppelkopf.persistence.model.player.PlayerEntity
import game.doppelkopf.persistence.model.user.UserEntity
import io.mockk.mockk
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameModelTest {
    @Nested
    inner class Start {
        @Test
        fun `start the game sets dealer and advances state`() {
            val game = GameModel(
                GameEntity(
                    creator = mockk(),
                    maxNumberOfPlayers = 8
                ).apply {
                    repeat(4) { players.add(PlayerEntity(user = mockk(), game = this, seat = it)) }
                }
            )

            assertThatCode {
                game.start()
            }.doesNotThrowAnyException()

            assertThat(game.state).isEqualTo(GameState.WAITING_FOR_DEAL)
            assertThat(game.started).isNotNull
            assertThat(game.players.count { !it.dealer }).isEqualTo(3)
            assertThat(game.players.count { it.dealer }).isEqualTo(1)
        }
    }

    @Nested
    inner class GetFourPlayersBehind {
        @ParameterizedTest
        @ValueSource(ints = [0, 1, 2, 3])
        fun `get four players behind returns in circular increasing seat position order for 4 players`(given: Int) {
            val game = createTestGameEntity()
            // Players are not assumed to be in correct order in the game entity, thus (6 - it).
            // Player at index 3 is the one with the lowest seat number here.
            val players =
                List(4) { PlayerEntity(user = mockk(), game = game.entity, seat = 6 - it) }
                    .map { PlayerModel(it) }.onEach { game.addPlayer(it) }

            val quad = game.getFourPlayersBehind(players[given])
            val seatCompare = quad.toList().zipWithNext { a, b -> a.seat < b.seat }
            // Due to circularity, depending on the starting position, we can not have more than 1 break of the seat
            // position order.
            assertThat(seatCompare.count { !it }).isEqualTo(
                when (given) {
                    0 -> 0 // without circular breaking
                    else -> 1 // In all other cases, we expect exactly one circular order break.
                }
            )

            // No player was let out in the selection
            assertThat(quad.toList().map { it.id }).containsExactlyInAnyOrderElementsOf(players.map { it.id })
            // Since we have 4 players, we expect the last one in the selection to be the given player.
            assertThat(quad.fourth).isEqualTo(players[given])
        }

        @ParameterizedTest
        @ValueSource(ints = [0, 1, 2, 3, 4])
        fun `get four players behind returns in circular increasing seat position order for 5 players`(given: Int) {
            val game = createTestGameEntity()
            // Simplify test by assume players are already in correct order here.
            val players = List(5) { PlayerEntity(user = mockk(), game = game.entity, seat = it) }
                .map { PlayerModel(it) }.onEach { game.addPlayer(it) }

            val quad = game.getFourPlayersBehind(players[given])
            val seatCompare = quad.toList().zipWithNext { a, b -> a.seat < b.seat }
            // Due to circularity, depending on the starting position, we can not have more than 1 break of the seat
            // position order.
            assertThat(seatCompare.count { !it }).isEqualTo(
                when (given) {
                    0, 4 -> 0 // without circular breaking
                    else -> 1 // In all other cases, we expect exactly one circular order break.
                }
            )

            // Since we have more than 4 players, the given one is left out of the selection.
            assertThat(quad.toList().map { it.id }).doesNotContain(players[given].id)
        }

        @ParameterizedTest
        @ValueSource(ints = [0, 1, 2, 3, 4, 5])
        fun `get four players behind returns in circular increasing seat position order for 6 players`(given: Int) {
            val game = createTestGameEntity()
            // Simplify test by assume players are already in correct order here.
            val players = List(6) { PlayerEntity(user = mockk(), game = game.entity, seat = it) }
                .map { PlayerModel(it) }.onEach { game.addPlayer(it) }

            val quad = game.getFourPlayersBehind(players[given])
            val seatCompare = quad.toList().zipWithNext { a, b -> a.seat < b.seat }
            // Due to circularity, depending on the starting position, we can not have more than 1 break of the seat
            // position order.
            assertThat(seatCompare.count { !it }).isEqualTo(
                when (given) {
                    0, 1, 5 -> 0 // without circular breaking
                    else -> 1 // In all other cases, we expect exactly one circular order break.
                }
            )

            // Since we have more than 4 players, the given one is left out of the selection.
            assertThat(quad.toList().map { it.id }).doesNotContain(players[given].id)
        }

        @ParameterizedTest
        @ValueSource(ints = [0, 1, 2, 3, 4, 5, 6])
        fun `get four players behind returns in circular increasing seat position order for 7 players`(given: Int) {
            val game = createTestGameEntity()
            // Simplify test by assume players are already in correct order here.
            val players = List(7) { PlayerEntity(user = mockk(), game = game.entity, seat = it) }
                .map { PlayerModel(it) }.onEach { game.addPlayer(it) }

            val quad = game.getFourPlayersBehind(players[given])
            val seatCompare = quad.toList().zipWithNext { a, b -> a.seat < b.seat }
            // Due to circularity, depending on the starting position, we can not have more than 1 break of the seat
            // position order.
            assertThat(seatCompare.count { !it }).isEqualTo(
                when (given) {
                    0, 1, 2, 6 -> 0 // without circular breaking
                    else -> 1 // In all other cases, we expect exactly one circular order break.
                }
            )
            // Since we have more than 4 players, the given one is left out of the selection.
            assertThat(quad.toList().map { it.id }).doesNotContain(players[given].id)

        }

        @ParameterizedTest
        @ValueSource(ints = [0, 1, 2, 3, 4, 5, 6, 7])
        fun `get four players behind returns in circular increasing seat position order for 8 players`(given: Int) {
            val game = createTestGameEntity()
            // Simplify test by assume players are already in correct order here.
            val players = List(8) { PlayerEntity(user = mockk(), game = game.entity, seat = it) }
                .map { PlayerModel(it) }.onEach { game.addPlayer(it) }

            val quad = game.getFourPlayersBehind(players[given])
            val seatCompare = quad.toList().zipWithNext { a, b -> a.seat < b.seat }
            // Due to circularity, depending on the starting position, we can not have more than 1 break of the seat
            // position order.
            assertThat(seatCompare.count { !it }).isEqualTo(
                when (given) {
                    0, 1, 2, 3, 7 -> 0 // 0,1 without circular breaking
                    else -> 1 // In all other cases, we expect exactly one circular order break.
                }
            )

            // Since we have more than 4 players, the given one is left out of the selection.
            assertThat(quad.toList().map { it.id }).doesNotContain(players[given].id)
        }

        @Test
        fun `get four players throws exception when given player is not at the game`() {
            val game = createTestGameEntity()
            List(4) { PlayerEntity(user = mockk(), game = game.entity, seat = it) }
                .map { PlayerModel(it) }.onEach { game.addPlayer(it) }

            val notGamePlayer = PlayerModel(PlayerEntity(user = mockk(), game = game.entity, seat = 17))

            assertThatThrownBy {
                game.getFourPlayersBehind(notGamePlayer)
            }.isInstanceOf(GameFailedException::class.java)
                .hasMessageContaining("Could not determine the position of PlayerEntity")
        }

        @ParameterizedTest
        @ValueSource(ints = [0, 1, 2, 3])
        fun `get four players behind fails when game has not at least 4 players`(playerCount: Int) {
            val game = createTestGameEntity()
            repeat(playerCount) { game.addPlayer(PlayerModel(mockk())) }

            assertThatThrownBy {
                game.getFourPlayersBehind(mockk())
            }.isInstanceOf(GameFailedException::class.java)
                .hasMessageContaining("Can not determine 4 players when game has only $playerCount players.")
        }
    }

    private fun createTestGameEntity(): GameModel {
        return GameEntity(
            creator = UserEntity(
                username = "username",
                password = "password"
            ),
            maxNumberOfPlayers = 4
        ).apply {
            state = GameState.INITIALIZED
        }.let { GameModel(it) }
    }
}