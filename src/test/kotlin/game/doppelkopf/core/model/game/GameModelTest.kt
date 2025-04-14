package game.doppelkopf.core.model.game

import game.doppelkopf.core.cards.Deck
import game.doppelkopf.core.cards.DeckMode
import game.doppelkopf.core.common.enums.GameState
import game.doppelkopf.core.common.enums.Team
import game.doppelkopf.core.common.errors.ForbiddenActionException
import game.doppelkopf.core.common.errors.GameFailedException
import game.doppelkopf.core.common.errors.InvalidActionException
import game.doppelkopf.core.model.player.PlayerModel
import game.doppelkopf.core.model.user.UserModel
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
    inner class Start {
        @Test
        fun `start the game sets dealer and advances state`() {
            val creator = UserModel.create(
                entity = createTestUserEntity()
            )

            val game = GameModel.create(
                entity = GameEntity(
                    creator = creator.entity,
                    maxNumberOfPlayers = 4
                ).apply {
                    repeat(4) { players.add(PlayerEntity(user = createTestUserEntity(), game = this, seat = it)) }
                }
            )

            assertThatCode {
                game.start(creator)
            }.doesNotThrowAnyException()

            assertThat(game.state).isEqualTo(GameState.WAITING_FOR_DEAL)
            assertThat(game.started).isNotNull
            assertThat(game.players.values.count { !it.dealer }).isEqualTo(3)
            assertThat(game.players.values.count { it.dealer }).isEqualTo(1)
        }

        @Test
        fun `guard yields exception when user is not the creator`() {
            val notCreator = UserModel.create(
                entity = createTestUserEntity()
            )
            val game = GameModel.create(
                entity = GameEntity(
                    creator = createTestUserEntity(),
                    maxNumberOfPlayers = 4
                )
            )

            val guard = game.canStart(notCreator)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(ForbiddenActionException::class.java)
                .hasMessageContaining("You are not allowed to perform the action 'Game:Start': Only the creator of the game can start it.")

            assertThatThrownBy {
                game.start(notCreator)
            }.isInstanceOf(ForbiddenActionException::class.java)
                .hasMessageContaining("You are not allowed to perform the action 'Game:Start': Only the creator of the game can start it.")
        }

        @ParameterizedTest
        @EnumSource(GameState::class, names = ["INITIALIZED"], mode = EnumSource.Mode.EXCLUDE)
        fun `guard yields exception when game is not in INITIALIZED state`(gameState: GameState) {
            val game = GameModel.create(
                entity = createTestGameEntity().apply {
                    state = gameState
                }
            )

            val guard = game.canStart(game.creator)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Start' can not be performed: The game has been started already.")

            assertThatThrownBy {
                game.start(game.creator)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Start' can not be performed: The game has been started already.")
        }

        @ParameterizedTest
        @ValueSource(ints = [0, 1, 2, 3])
        fun `guard yields exception when game has less than 4 players`(playerCount: Int) {
            val game = GameModel.create(
                entity = createTestGameEntity().apply {
                    repeat(playerCount) {
                        players.add(PlayerEntity(user = createTestUserEntity(), game = this, seat = it))
                    }
                }
            )

            val guard = game.canStart(game.creator)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Start' can not be performed: The game needs to have at least 4 players.")

            assertThatThrownBy {
                game.start(game.creator)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Game:Start' can not be performed: The game needs to have at least 4 players.")
        }
    }

    @Nested
    inner class Join {
        @Test
        fun `creates player and assigns it to the game`() {
            val game = GameModel.create(
                entity = createTestGameEntity().apply {
                    state = GameState.INITIALIZED
                }
            )

            repeat(4) {
                // Game allows for 4 players, so we join the game with 4 unique users at 4 different seats for this test.
                val user = UserModel.create(
                    entity = createTestUserEntity()
                )

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
            val user = UserModel.create(
                entity = createTestUserEntity()
            )

            val game = GameModel.create(
                entity = createTestGameEntity().apply { state = gameState }
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
            val user = UserModel.create(
                entity = createTestUserEntity()
            )
            val game = GameModel.create(
                entity = createTestGameEntity()
            ).apply {
                addPlayer(PlayerModel.create(PlayerEntity(user = user.entity, game = this.entity, seat = 0)))
            }

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
            val user = UserModel.create(
                entity = createTestUserEntity()
            )
            val game = GameModel.create(
                entity = createTestGameEntity()
            )

            game.addPlayer(
                PlayerModel.create(
                    entity = PlayerEntity(
                        user = createTestUserEntity(),
                        game = game.entity,
                        seat = 0
                    )
                )
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
            val user = UserModel.create(
                entity = createTestUserEntity()
            )
            val game = GameModel.create(
                entity = createTestGameEntity()
            ).apply {
                repeat(4) {
                    addPlayer(
                        PlayerModel.create(
                            entity = PlayerEntity(user = createTestUserEntity(), game = entity, seat = it)
                        )
                    )
                }
            }

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

    @Nested
    inner class Deal {
        @Test
        fun `creates round and assigns it to the game, creates hands and assigns them to the round and players`() {
            val game = GameModel.create(
                entity = createTestGameEntity().apply {
                    state = GameState.WAITING_FOR_DEAL
                }
            )
            val players = List(4) {
                PlayerEntity(user = createTestUserEntity(), game = game.entity, seat = it).apply { dealer = false }
            }.let { pList -> PlayerModel.create(pList) }.onEach { game.addPlayer(it) }

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
            val game = GameModel.create(
                entity = createTestGameEntity().apply {
                    state = gameState
                }
            )
            val user = UserModel.create(entity = createTestUserEntity())

            val guard = game.canDeal(user = user)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Round:Create' can not be performed: The game is currently not in the ${GameState.WAITING_FOR_DEAL} state.")

            assertThatThrownBy {
                game.deal(user = user)
            }.isInstanceOf(InvalidActionException::class.java)
                .hasMessageContaining("The action 'Round:Create' can not be performed: The game is currently not in the ${GameState.WAITING_FOR_DEAL} state.")
        }

        @Test
        fun `guard yields exception when user is not the dealer`() {
            val game = GameModel.create(
                entity = createTestGameEntity().apply {
                    state = GameState.WAITING_FOR_DEAL
                }
            )
            val player = PlayerModel.create(
                entity = PlayerEntity(game = game.entity, user = createTestUserEntity(), seat = 0)
            ).apply { dealer = false }

            val guard = game.canDeal(user = player.user)
            assertThat(guard.isFailure).isTrue

            assertThat(guard.exceptionOrNull())
                .isInstanceOf(ForbiddenActionException::class.java)
                .hasMessageContaining("You are not allowed to perform the action 'Round:Create': Only the current dealer of the game can deal this round.")

            assertThatThrownBy {
                game.deal(user = player.user)
            }.isInstanceOf(ForbiddenActionException::class.java)
                .hasMessageContaining("You are not allowed to perform the action 'Round:Create': Only the current dealer of the game can deal this round.")
        }

        private fun createTestGameEntity(): GameEntity {
            return GameEntity(
                creator = createTestUserEntity(),
                maxNumberOfPlayers = 4
            ).apply {
                state = GameState.INITIALIZED
            }
        }

        private fun createTestUserEntity(): UserEntity {
            return UserEntity(username = "username", password = "password")
        }
    }

    @Nested
    inner class GetFourPlayersBehind {
        @ParameterizedTest
        @ValueSource(ints = [0, 1, 2, 3])
        fun `get four players behind returns in circular increasing seat position order for 4 players`(given: Int) {
            val game = GameModel.create(
                entity = createTestGameEntity()
            )
            // Players are not assumed to be in correct order in the game entity, thus (6 - it).
            // Player at index 3 is the one with the lowest seat number here.
            val players =
                List(4) { PlayerEntity(user = createTestUserEntity(), game = game.entity, seat = 6 - it) }
                    .map { PlayerModel.create(it) }.onEach { game.addPlayer(it) }

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
            val game = GameModel.create(
                entity = createTestGameEntity()
            )
            // Simplify test by assume players are already in correct order here.
            val players = List(5) { PlayerEntity(user = createTestUserEntity(), game = game.entity, seat = it) }
                .map { PlayerModel.create(it) }.onEach { game.addPlayer(it) }

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
            val game = GameModel.create(
                entity = createTestGameEntity()
            )
            // Simplify test by assume players are already in correct order here.
            val players = List(6) { PlayerEntity(user = createTestUserEntity(), game = game.entity, seat = it) }
                .map { PlayerModel.create(it) }.onEach { game.addPlayer(it) }

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
            val game = GameModel.create(
                entity = createTestGameEntity()
            )
            // Simplify test by assume players are already in correct order here.
            val players = List(7) { PlayerEntity(user = createTestUserEntity(), game = game.entity, seat = it) }
                .map { PlayerModel.create(it) }.onEach { game.addPlayer(it) }

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
            val game = GameModel.create(
                entity = createTestGameEntity()
            )
            // Simplify test by assume players are already in correct order here.
            val players = List(8) { PlayerEntity(user = createTestUserEntity(), game = game.entity, seat = it) }
                .map { PlayerModel.create(it) }.onEach { game.addPlayer(it) }

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
            val game = GameModel.create(
                entity = createTestGameEntity()
            )
            List(4) { PlayerEntity(user = createTestUserEntity(), game = game.entity, seat = it) }
                .map { PlayerModel.create(it) }.onEach { game.addPlayer(it) }

            val notGamePlayer = PlayerModel.create(
                entity = PlayerEntity(user = createTestUserEntity(), game = game.entity, seat = 17)
            )

            assertThatThrownBy {
                game.getFourPlayersBehind(notGamePlayer)
            }.isInstanceOf(GameFailedException::class.java)
                .hasMessageContaining("Could not determine the position of PlayerEntity")
        }

        @ParameterizedTest
        @ValueSource(ints = [0, 1, 2, 3])
        fun `get four players behind fails when game has not at least 4 players`(playerCount: Int) {
            val game = GameModel.create(
                entity = createTestGameEntity()
            )

            repeat(playerCount) {
                PlayerModel.create(
                    PlayerEntity(user = createTestUserEntity(), game = game.entity, seat = it),
                ).also { p -> game.addPlayer(p) }
            }

            assertThatThrownBy {
                game.getFourPlayersBehind(mockk())
            }.isInstanceOf(GameFailedException::class.java)
                .hasMessageContaining("Can not determine 4 players when game has only $playerCount players.")
        }
    }

    @Nested
    inner class Create {
        @Test
        fun `create uses one model per entity`() {
            val entity = GameEntity(
                creator = mockk(),
                maxNumberOfPlayers = 4
            )

            val model = GameModel.create(entity)
            val other = GameModel.create(entity)

            assertThat(model).isSameAs(other)
        }
    }

    private fun createTestUserEntity(): UserEntity {
        return UserEntity(username = "username", password = "password")
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