package game.doppelkopf.api.game

import game.doppelkopf.BaseRestAssuredTest
import game.doppelkopf.api.game.dto.PlayerCreateDto
import game.doppelkopf.api.game.dto.PlayerInfoDto
import game.doppelkopf.core.game.GameState
import game.doppelkopf.errors.ProblemDetailResponse
import game.doppelkopf.persistence.game.GameEntity
import game.doppelkopf.persistence.game.GameRepository
import game.doppelkopf.persistence.game.PlayerEntity
import game.doppelkopf.persistence.game.PlayerRepository
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class PlayerControllerTest : BaseRestAssuredTest() {
    @Autowired
    private lateinit var gameRepository: GameRepository

    @Autowired
    private lateinit var playerRepository: PlayerRepository

    @Test
    @Order(1)
    fun `get list of players when user has no players returns 200 empty list`() {
        val response = Given {
            this
        } When {
            get("/v1/players")
        } Then {
            statusCode(200)
        } Extract {
            response().jsonPath().getList<PlayerInfoDto>("$")
        }

        assertThat(response).hasSize(1)
    }

    @Test
    @Order(2)
    fun `get specific with unknown uuid returns 404`() {
        val response = Given {
            this
        } When {
            get("/v1/players/$zeroId")
        } Then {
            statusCode(404)
        } Extract {
            response().`as`(ProblemDetailResponse::class.java)
        }

        assertThat(response.instance.toString()).isEqualTo("/v1/players/$zeroId")
        assertThat(response.title).isEqualTo("Entity not found")
        assertThat(response.detail).isEqualTo("The entity of type PlayerEntity with id $zeroId was not found.")
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3, 4, 5, 6, 7])
    @Order(4)
    fun `join game at valid seat position returns 201 and dto`(seat: Int) {
        val game = gameRepository.saveAndFlush(createGameOfAdminUser())

        val response = execJoinRequest<PlayerInfoDto>(game.id, seat, 201)

        assertThat(response.user.id).isEqualTo(testUser.id)
        assertThat(response.seat).isEqualTo(seat)
        assertThat(response.gameId).isEqualTo(game.id)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 8, -17, 42])
    @Order(5)
    fun `join game at invalid seat position returns 400 bad request`(seat: Int) {
        val game = gameRepository.saveAndFlush(createGameOfAdminUser())

        val response = execJoinRequest<ProblemDetailResponse>(game.id, seat, 400)

        assertThat(response.instance.toString()).isEqualTo("/v1/players")
        assertThat(response.title).isEqualTo("Bad Request")
        assertThat(response.detail).isEqualTo("Invalid request content.")
    }

    @Test
    @Order(6)
    fun `joining a full table returns 400 bad request`() {
        val game = gameRepository.saveAndFlush(
            createGameOfAdminUser().apply {
                repeat(3) {
                    players.add(PlayerEntity(user = testPlayers[it], game = this, seat = it + 1))
                }
            }
        )

        val response = execJoinRequest<ProblemDetailResponse>(game.id, 7, 400)

        assertThat(response.instance.toString()).isEqualTo("/v1/players")
        assertThat(response.title).isEqualTo("Invalid action")
        assertThat(response.detail).isEqualTo("The action 'Game:Join' can not be performed: This game is already at its maximum capacity.")
    }

    @Test
    @Order(7)
    fun `joining at a already taken seat position returns 400 bad request`() {
        val game = gameRepository.saveAndFlush(
            createGameOfAdminUser().apply {
                players.add(
                    PlayerEntity(
                        user = testPlayers[0],
                        game = this,
                        seat = 3
                    )
                )
            }
        )

        val response = execJoinRequest<ProblemDetailResponse>(game.id, 3, 400)

        assertThat(response.instance.toString()).isEqualTo("/v1/players")
        assertThat(response.title).isEqualTo("Invalid action")
        assertThat(response.detail).isEqualTo("The action 'Game:Join' can not be performed: The seat you have chosen is already taken by another player.")
    }

    @Test
    @Order(8)
    fun `joining an already started game returns 400 bad request`() {
        val game = gameRepository.saveAndFlush(
            createGameOfAdminUser().apply {
                state = GameState.WAITING_FOR_DEAL
            }
        )

        val response = execJoinRequest<ProblemDetailResponse>(game.id, 7, 400)

        assertThat(response.instance.toString()).isEqualTo("/v1/players")
        assertThat(response.title).isEqualTo("Invalid action")
        assertThat(response.detail).isEqualTo("The action 'Game:Join' can not be performed: You can not join a game that has already started.")
    }

    @Test
    @Order(9)
    fun `joining game that does not exist returns 404`() {
        val response =
            execJoinRequest<ProblemDetailResponse>(zeroId, 3, 404)


    }

    @BeforeAll
    fun `prepare games as admin user in the database so its not empty`() {
        gameRepository.saveAndFlush(createGameOfAdminUser())
    }

    final inline fun <reified T> execJoinRequest(
        gameId: UUID,
        seat: Int,
        expectedStatus: Int,
    ): T {
        return Given {
            contentType(ContentType.JSON)
            body(
                PlayerCreateDto(
                    gameId = gameId,
                    seat = seat
                )
            )
        } When {
            post("/v1/players")
        } Then {
            statusCode(expectedStatus)
        } Extract {
            response().`as`(T::class.java)
        }
    }

    private fun createGameOfAdminUser(): GameEntity {
        return GameEntity(
            creator = testAdmin,
            maxNumberOfPlayers = 4
        ).apply {
            players.add(
                PlayerEntity(
                    user = testAdmin,
                    game = this,
                    seat = 0
                )
            )
        }
    }
}