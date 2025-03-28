package game.doppelkopf.api.game

import game.doppelkopf.BaseRestAssuredTest
import game.doppelkopf.api.game.dto.GameCreateDto
import game.doppelkopf.api.game.dto.GameInfoDto
import game.doppelkopf.errors.ProblemDetailResponse
import game.doppelkopf.persistence.game.GameEntity
import game.doppelkopf.persistence.game.GameRepository
import game.doppelkopf.persistence.game.PlayerEntity
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class GameControllerTest : BaseRestAssuredTest() {
    @Autowired
    private lateinit var gameRepository: GameRepository

    @Test
    @Order(1)
    fun `get list with no games in database returns 200 empty list`() {
        val response = Given {
            this
        } When {
            get("/v1/games")
        } Then {
            statusCode(200)
        } Extract {
            response().jsonPath().getList<GameInfoDto>("$")
        }

        assertThat(response).isEmpty()
    }

    @Test
    @Order(2)
    fun `get specific with unknown uuid returns 404`() {
        val response = Given {
            this
        } When {
            get("/v1/games/00000000-0000-0000-0000-000000000000")
        } Then {
            statusCode(404)
        } Extract {
            response().`as`(ProblemDetailResponse::class.java)
        }

        assertThat(response.instance.toString()).isEqualTo("/v1/games/00000000-0000-0000-0000-000000000000")
        assertThat(response.title).isEqualTo("Entity not found")
        assertThat(response.detail).isEqualTo("The entity of type GameEntity with id 00000000-0000-0000-0000-000000000000 was not found.")
    }

    @ParameterizedTest
    @ValueSource(ints = [4, 5, 6, 7, 8])
    @Order(3)
    fun `create with valid player limit returns 201 and dto`(playerLimit: Int) {
        val response = Given {
            contentType(ContentType.JSON)
            body(
                GameCreateDto(
                    playerLimit = playerLimit,
                )
            )
        } When {
            post("/v1/games")
        } Then {
            statusCode(201)
        } Extract {
            response().`as`(GameInfoDto::class.java)
        }

        assertThat(response.creator.id).isEqualTo(testUser.id)
        // The default login for the testing is testUser.
        assertThat(response.playerLimit).isEqualTo(playerLimit)
        assertThat(response.players).hasSize(1)
        assertThat(response.players.map { it.user.id }).containsExactly(testUser.id)
        assertThat(response.players.map { it.seat }).containsExactly(0)
        // Ensure that the creator was automatically added as player in seat position 0.
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3, -17, 42])
    @Order(4)
    fun `create with invalid player limit returns 400 bad request`(playerLimit: Int) {
        val response = Given {
            contentType(ContentType.JSON)
            body(
                GameCreateDto(
                    playerLimit = playerLimit,
                )
            )
        } When {
            post("/v1/games")
        } Then {
            statusCode(400)
        } Extract {
            response().`as`(ProblemDetailResponse::class.java)
        }

        assertThat(response.instance.toString()).isEqualTo("/v1/games")
        assertThat(response.title).isEqualTo("Bad Request")
        assertThat(response.detail).isEqualTo("Invalid request content.")
    }

    @Test
    @Order(4)
    fun `get list with games in database returns 200 and list of dto`() {
        val response = Given {
            this
        } When {
            get("/v1/games")
        } Then {
            statusCode(200)
        } Extract {
            response().jsonPath().getList<GameInfoDto>("$")
        }

        assertThat(response).hasSize(5)
        // the previous test should have created 5 games
    }

    @Test
    @Order(5)
    fun `get specific by its id returns 200 and dto`() {
        val entity = GameEntity(
            creator = testAdmin,
            maxNumberOfPlayers = 8
        ).apply {
            testPlayers.forEachIndexed { index, player ->
                players.add(
                    PlayerEntity(
                        user = player,
                        game = this,
                        seat = index
                    )
                )
            }
        }.let { gameRepository.save(it) }

        val response = Given {
            this
        } When {
            get("/v1/games/${entity.id}")
        } Then {
            statusCode(200)
        } Extract {
            response().`as`(GameInfoDto::class.java)
        }

        assertThat(response.id).isEqualTo(entity.id)
        assertThat(response.creator.id).isEqualTo(entity.creator.id)
        assertThat(response.playerLimit).isEqualTo(entity.players.size)
        assertThat(
            response.players.map { it.user.id }
        ).containsExactlyInAnyOrderElementsOf(testPlayers.map { it.id })
    }
}