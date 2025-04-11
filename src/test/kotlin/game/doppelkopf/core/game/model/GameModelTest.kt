package game.doppelkopf.core.game.model

import game.doppelkopf.core.common.errors.ForbiddenActionException
import game.doppelkopf.core.common.errors.GameFailedException
import game.doppelkopf.core.common.errors.InvalidActionException
import game.doppelkopf.core.common.enums.GameState
import game.doppelkopf.core.common.enums.RoundState
import game.doppelkopf.persistence.model.game.GameEntity
import game.doppelkopf.persistence.model.player.PlayerEntity
import game.doppelkopf.persistence.model.user.UserEntity
import io.mockk.mockk
import org.assertj.core.api.Assertions.*
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
        @Test
        fun `start sets dealer and advances state`() {
            val user = UserEntity(username = "username", password = "password")
            val game = GameEntity(
                creator = user,
                maxNumberOfPlayers = 8
            ).apply {
                state = GameState.INITIALIZED
                repeat(4) { players.add(PlayerEntity(user = mockk(), game = this, seat = it + 1)) }
            }

            assertThatCode {
                GameModel(game).start(user)
            }.doesNotThrowAnyException()

            assertThat(game.state).isEqualTo(GameState.WAITING_FOR_DEAL)
            assertThat(game.started).isNotNull
            assertThat(game.players.count { it.dealer }).isEqualTo(1)
        }

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
        @Test
        fun `join creates PlayerEntity and assigns it to the game`() {
            val user = UserEntity(username = "username", password = "password")
            val game = createTestGameEntity()

            val player = GameModel(game).join(user, 7)

            assertThat(game.players).hasSize(1)
            // the test game entity has no players by default

            assertThat(game.players).contains(player)
            assertThat(player.user).isEqualTo(user)
            assertThat(player.game).isEqualTo(game)
            assertThat(player.seat).isEqualTo(7)
            assertThat(player.dealer).isFalse()
        }

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
        @Test
        fun `dealNextRound returns RoundEntity and assigns it to the game`() {
            val game = createTestGameEntity().apply {
                state = GameState.WAITING_FOR_DEAL
            }
            val players = List(4) {
                PlayerEntity(
                    user = UserEntity(username = "username", password = "password"),
                    game = game,
                    seat = it
                ).apply {
                    dealer = false
                }
            }
            val dealer = players.first().apply {
                dealer = true
            }

            game.players.addAll(players)

            val round = GameModel(game).dealNextRound(dealer.user)

            assertThat(game.state).isEqualTo(GameState.PLAYING_ROUND)

            assertThat(round.game).isEqualTo(game)
            assertThat(round.number).isEqualTo(1)
            assertThat(round.dealer).isEqualTo(dealer)
            assertThat(round.state).isEqualTo(RoundState.INITIALIZED)
            assertThat(round.started).isNotNull()
        }

        @ParameterizedTest
        @EnumSource(value = GameState::class, names = ["WAITING_FOR_DEAL"], mode = EnumSource.Mode.EXCLUDE)
        fun `next round throws exception when game is not in correct state`(gameState: GameState) {
            val game = createTestGameEntity().apply {
                state = gameState
            }

            assertThatThrownBy {
                GameModel(game).dealNextRound(UserEntity(username = "username", password = "password"))
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
                GameModel(game).dealNextRound(UserEntity(username = "username", password = "password"))
            }.isInstanceOf(ForbiddenActionException::class.java)
                .hasMessageContaining("You are not allowed to perform the action 'Round:Create': Only the current dealer of the game can start this round.")
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
            val players = List(4) { PlayerEntity(user = mockk(), game = game, seat = 6 - it) }.onEach {
                game.players.add(it)
            }

            val quad = GameModel(game).getFourPlayersBehind(players[given])
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
            val players = List(5) { PlayerEntity(user = mockk(), game = game, seat = it) }.onEach {
                game.players.add(it)
            }

            val quad = GameModel(game).getFourPlayersBehind(players[given])
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
            val players = List(6) { PlayerEntity(user = mockk(), game = game, seat = it) }.onEach {
                game.players.add(it)
            }

            val quad = GameModel(game).getFourPlayersBehind(players[given])
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
            val players = List(7) { PlayerEntity(user = mockk(), game = game, seat = it) }.onEach {
                game.players.add(it)
            }

            val quad = GameModel(game).getFourPlayersBehind(players[given])
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
            val players = List(8) { PlayerEntity(user = mockk(), game = game, seat = it) }.onEach {
                game.players.add(it)
            }

            val quad = GameModel(game).getFourPlayersBehind(players[given])
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
        fun `get four players behind fails game when given player not at the game`() {
            val game = createTestGameEntity()
            repeat(4) { game.players.add(PlayerEntity(user = mockk(), game = game, seat = it)) }

            assertThatThrownBy {
                GameModel(game).getFourPlayersBehind(PlayerEntity(user = mockk(), game = game, seat = 17))
            }.isInstanceOf(GameFailedException::class.java)
                .hasMessageContaining("Could not determine the position of PlayerEntity")
        }

        @ParameterizedTest
        @ValueSource(ints = [0, 1, 2, 3])
        fun `get four players behind fails when game has not at least 4 players`(playerCount: Int) {
            val game = createTestGameEntity()
            repeat(playerCount) { game.players.add(mockk()) }

            assertThatThrownBy {
                GameModel(game).getFourPlayersBehind(mockk())
            }.isInstanceOf(GameFailedException::class.java)
                .hasMessageContaining("Can not determine 4 players when game has only $playerCount players.")
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