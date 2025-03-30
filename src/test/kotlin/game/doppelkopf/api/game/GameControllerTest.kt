package game.doppelkopf.api.game

import game.doppelkopf.BaseRestAssuredTest
import game.doppelkopf.api.game.dto.GameCreateDto
import game.doppelkopf.api.game.dto.GameInfoDto
import game.doppelkopf.api.game.dto.GameOperationDto
import game.doppelkopf.core.errors.ForbiddenActionException
import game.doppelkopf.core.errors.InvalidActionException
import game.doppelkopf.core.game.model.GameModelFactory
import game.doppelkopf.core.game.model.GameOperation
import game.doppelkopf.errors.ProblemDetailResponse
import game.doppelkopf.persistence.game.GameEntity
import game.doppelkopf.persistence.game.GameRepository
import game.doppelkopf.persistence.game.PlayerEntity
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.convention.TestBean
import java.util.*

class GameControllerTest : BaseRestAssuredTest() {
    @Autowired
    private lateinit var gameRepository: GameRepository

    @TestBean
    private lateinit var gameModelFactory: GameModelFactory

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun gameModelFactory(): GameModelFactory {
            return mockk<GameModelFactory>()
        }
    }

    @Test
    fun `get list with no games in database returns 200 empty list`() {
        gameRepository.deleteAll()

        val response = getResourceList<GameInfoDto>("/v1/games", 200)

        assertThat(response).isEmpty()
    }

    @Test
    fun `get list with some games in the database returns 200 with list of dto`() {
        gameRepository.deleteAll()
        val entities = listOf(
            GameEntity(creator = testUser, maxNumberOfPlayers = 4),
            GameEntity(creator = testUser, maxNumberOfPlayers = 5),
            GameEntity(creator = testAdmin, maxNumberOfPlayers = 6),
            GameEntity(creator = testAdmin, maxNumberOfPlayers = 7),
        ).map {
            gameRepository.save(it)
        }

        val response = getResourceList<GameInfoDto>("/v1/games", 200)
        assertThat(response).hasSize(4)

        assertThat(response.map { it.id }).containsExactlyInAnyOrderElementsOf(entities.map { it.id })
    }

    @Test
    fun `get specific with unknown uuid returns 404`() {
        val response = getResource<ProblemDetailResponse>(
            path = "/v1/games/$zeroId",
            expectedStatus = 404
        )

        assertThat(response.instance.toString()).isEqualTo("/v1/games/$zeroId")
        assertThat(response.title).isEqualTo("Entity not found")
        assertThat(response.detail).isEqualTo("The entity of type GameEntity with id $zeroId was not found.")
    }

    @ParameterizedTest
    @ValueSource(ints = [4, 5, 6, 7, 8])
    fun `create with valid player limit returns 201 and dto`(playerLimit: Int) {
        val (response, location) = execCreateGame<GameInfoDto>(playerLimit, 201)

        response.also {
            assertThat(it.creator.id).isEqualTo(testUser.id)
            assertThat(it.playerLimit).isEqualTo(playerLimit)

            // Ensure that the creator was automatically added as player in seat position 0.
            assertThat(it.players).hasSize(1)
            assertThat(it.players.map { p -> p.user.id }).containsExactly(testUser.id)
            assertThat(it.players.map { p -> p.seat }).containsExactly(0)
        }


        assertThat(location).isNotNull()

        getResource<GameInfoDto>(location!!, 200).also {
            assertThat(it.creator.id).isEqualTo(testUser.id)
            assertThat(it.playerLimit).isEqualTo(playerLimit)

            // Ensure that the creator was automatically added as player in seat position 0.
            assertThat(it.players).hasSize(1)
            assertThat(it.players.map { p -> p.user.id }).containsExactly(testUser.id)
            assertThat(it.players.map { p -> p.seat }).containsExactly(0)
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3, -17, 42])
    fun `create with invalid player limit returns 400 bad request`(playerLimit: Int) {
        val (response, location) = execCreateGame<ProblemDetailResponse>(playerLimit, 400)

        assertThat(response.instance.toString()).isEqualTo("/v1/games")
        assertThat(response.title).isEqualTo("Bad Request")
        assertThat(response.detail).isEqualTo("Invalid request content.")

        assertThat(location).isNull()
    }

    @Test
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

        val response = getResource<GameInfoDto>(
            path = "/v1/games/${entity.id}",
            expectedStatus = 200
        )

        assertThat(response.id).isEqualTo(entity.id)
        assertThat(response.creator.id).isEqualTo(entity.creator.id)
        assertThat(response.playerLimit).isEqualTo(entity.players.size)
        assertThat(
            response.players.map { it.user.id }
        ).containsExactlyInAnyOrderElementsOf(testPlayers.map { it.id })
    }

    @Test
    fun `start returns 400 when invalid action exception`() {
        every { gameModelFactory.create(any()) } returns mockk {
            every { start(testUser) } throws InvalidActionException(
                "Game:Start",
                "Mocked Model Exception."
            )
        }

        val game = GameEntity(creator = testUser, maxNumberOfPlayers = 4).let {
            gameRepository.save(it)
        }

        val response = execPatchGame<ProblemDetailResponse>(game.id, GameOperation.START, 400)

        response.also {
            assertThat(response.instance.toString()).isEqualTo("/v1/games/${game.id}")
            assertThat(response.title).isEqualTo("Invalid action")
            assertThat(response.detail).isEqualTo("The action 'Game:Start' can not be performed: Mocked Model Exception.")
        }
    }

    @Test
    fun `start returns 403 when forbidden action exception`() {
        every { gameModelFactory.create(any()) } returns mockk {
            every { start(testUser) } throws ForbiddenActionException(
                "Game:Start",
                "Mocked Model Exception."
            )
        }

        val game = GameEntity(creator = testUser, maxNumberOfPlayers = 4).let {
            gameRepository.save(it)
        }

        val response = execPatchGame<ProblemDetailResponse>(game.id, GameOperation.START, 403)

        response.also {
            assertThat(response.instance.toString()).isEqualTo("/v1/games/${game.id}")
            assertThat(response.title).isEqualTo("Forbidden action")
            assertThat(response.detail).isEqualTo("You are not allowed to perform the action 'Game:Start': Mocked Model Exception.")
        }
    }

    private final inline fun <reified T> execCreateGame(
        playerLimit: Int,
        expectedStatus: Int,
    ): Pair<T, String?> {
        return createResource<GameCreateDto, T>(
            path = "/v1/games",
            body = GameCreateDto(
                playerLimit = playerLimit,
            ),
            expectedStatus = expectedStatus,
        )
    }

    private final inline fun <reified T> execPatchGame(
        gameId: UUID,
        operation: GameOperation,
        expectedStatus: Int,
    ): T {
        return patchResource<GameOperationDto, T>(
            path = "/v1/games/$gameId",
            body = GameOperationDto(
                op = operation
            ),
            expectedStatus = expectedStatus,
        )
    }
}