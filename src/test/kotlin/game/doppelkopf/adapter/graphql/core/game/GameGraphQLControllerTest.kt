package game.doppelkopf.adapter.graphql.core.game

import game.doppelkopf.BaseGraphQLTest
import game.doppelkopf.adapter.graphql.core.game.dto.GameResponse
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.game.GameRepository
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.adapter.persistence.model.player.PlayerRepository
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.common.errors.ForbiddenActionException
import game.doppelkopf.common.errors.InvalidActionException
import game.doppelkopf.domain.game.enums.GameState
import game.doppelkopf.domain.game.service.GameStartModel
import game.doppelkopf.fragName
import game.doppelkopf.toSingleEntity
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.test.tester.GraphQlTester
import java.util.*

class GameGraphQLControllerTest : BaseGraphQLTest() {
    @Autowired
    private lateinit var gameRepository: GameRepository

    @Autowired
    private lateinit var playerRepository: PlayerRepository

    @BeforeEach
    @AfterEach
    fun `unmock all`() {
        clearAllMocks()
        unmockkAll()
    }

    @Nested
    inner class Queries {
        @Test
        fun `get list with no games in database returns empty list`() {
            gameRepository.deleteAll()

            val response = getGameList()
                .execute()
                .path("games")
                .entityList(GameResponse::class.java)
                .get()

            assertThat(response).isEmpty()
        }

        @Test
        fun `get list with some games in database returns list of games`() {
            gameRepository.deleteAll()
            val entities =
                listOf(testUser, testUser, testAdmin, testAdmin).map {
                    gameRepository.save(createGameOfUser(it))
                }

            val response = getGameList()
                .execute()
                .path("games")
                .entityList(GameResponse::class.java)
                .get()

            assertThat(response).hasSize(entities.size)
            assertThat(response.map { g -> g.id }).containsExactlyInAnyOrderElementsOf(entities.map { g -> g.id })
        }

        @Test
        fun `get specific with unknown uuid returns error`() {
            getGame(zeroId)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Entity not found")
                }.verify()
        }

        @Test
        fun `get specific with known uuid returns data`() {
            val entity = gameRepository.save(createGameOfUser(testUser))

            val response = getGame(entity.id)
                .execute()
                .toSingleEntity<GameResponse>()

            assertThat(response.id).isEqualTo(entity.id)
            assertThat(response.playerLimit).isEqualTo(entity.maxNumberOfPlayers)
            assertThat(response.cu.created).isEqualTo(entity.created)
            assertThat(response.cu.updated).isEqualTo(entity.updated)
        }
    }

    @Nested
    inner class Mutations {
        @ParameterizedTest
        @ValueSource(ints = [4, 5, 6, 7, 8])
        fun `create with valid player limit creates and returns data`(playerLimit: Int) {
            val response = createGame(playerLimit)
                .execute()
                .toSingleEntity<GameResponse>()

            assertThat(response.playerLimit).isEqualTo(playerLimit)
        }

        @ParameterizedTest
        @ValueSource(ints = [0, 1, 2, 3, -17, 42])
        fun `create with invalid player limit returns error`(playerLimit: Int) {
            createGame(playerLimit)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Validation failed")
                }.verify()
        }

        @Test
        fun `start game modifies game state and returns game when valid`() {
            val game = createGameOfUser(testUser).let {
                gameRepository.save(it)
            }.apply {
                repeat(3) {
                    players.add(
                        PlayerEntity(
                            user = testPlayers.filter { u -> u.id != testUser.id }[it],
                            game = this,
                            seat = it + 1
                        )
                    )
                }

                playerRepository.saveAll(players)
            }

            val response = startGame(game.id)
                .execute()
                .toSingleEntity<GameResponse>()

            assertThat(response.id).isEqualTo(game.id)
            assertThat(response.playerLimit).isEqualTo(game.maxNumberOfPlayers)
            assertThat(response.state).isEqualTo(GameState.WAITING_FOR_DEAL)
        }

        @Test
        fun `start game returns error when forbidden action exception`() {
            val game = createGameOfUser(testUser).let {
                gameRepository.save(it)
            }

            mockkConstructor(GameStartModel::class)
            every { anyConstructed<GameStartModel>().start(any()) } throws ForbiddenActionException(
                "Mocked Model Exception."
            )

            startGame(game.id)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Forbidden action")
                }.verify()
        }

        @Test
        fun `start game returns error when invalid action exception`() {
            val game = createGameOfUser(testUser).let {
                gameRepository.save(it)
            }

            mockkConstructor(GameStartModel::class)
            every { anyConstructed<GameStartModel>().start(any()) } throws InvalidActionException(
                "Mocked Model Exception."
            )

            startGame(game.id)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Invalid action")
                }.verify()
        }
    }

    private fun getGameList(): GraphQlTester.Request<*> {
        return gqlUserTester
            .documentName("getGameList")
            .fragName("gameProperties")
            .fragName("cu")
            .fragName("se")
    }

    private fun getGame(gameId: UUID): GraphQlTester.Request<*> {
        return gqlUserTester
            .documentName("getGame")
            .fragName("gameProperties")
            .fragName("cu")
            .fragName("se")
            .variable("id", gameId)
    }

    private fun createGame(playerLimit: Int): GraphQlTester.Request<*> {
        return gqlUserTester
            .documentName("createGame")
            .fragName("gameProperties")
            .fragName("cu")
            .fragName("se")
            .variable("playerLimit", playerLimit)
    }

    private fun startGame(gameId: UUID): GraphQlTester.Request<*> {
        return gqlUserTester
            .documentName("startGame")
            .fragName("gameProperties")
            .fragName("cu")
            .fragName("se")
            .variable("gameId", gameId)
    }

    private fun createGameOfUser(userEntity: UserEntity): GameEntity {
        return GameEntity(
            creator = userEntity,
            maxNumberOfPlayers = 4,
            seed = ByteArray(256),
        ).apply {
            players.add(
                PlayerEntity(
                    user = userEntity, game = this, seat = 0
                )
            )
        }
    }
}