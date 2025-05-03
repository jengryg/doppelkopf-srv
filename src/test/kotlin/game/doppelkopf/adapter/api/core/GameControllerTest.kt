package game.doppelkopf.adapter.api.core

import game.doppelkopf.BaseRestAssuredTest
import game.doppelkopf.adapter.api.core.game.dto.GameCreateDto
import game.doppelkopf.adapter.api.core.game.dto.GameInfoDto
import game.doppelkopf.adapter.api.core.game.dto.GameOperationDto
import game.doppelkopf.core.common.enums.GameOperation
import game.doppelkopf.core.errors.ForbiddenActionException
import game.doppelkopf.core.errors.InvalidActionException
import game.doppelkopf.core.model.game.handler.GameStartModel
import game.doppelkopf.errors.ProblemDetailResponse
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.game.GameRepository
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class GameControllerTest : BaseRestAssuredTest() {
    @Autowired
    private lateinit var gameRepository: GameRepository

    @BeforeEach
    @AfterEach
    fun `unmock all`() {
        clearAllMocks()
        unmockkAll()
    }

    @Nested
    inner class GetAndList {
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
                createGameOfUser(testUser),
                createGameOfUser(testUser),
                createGameOfUser(testAdmin),
                createGameOfUser(testAdmin)
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

        @Test
        fun `get specific by its id returns 200 and dto`() {
            val entity = createGameOfUser(testUser).apply {
                players.add(PlayerEntity(user = testPlayers[0], game = this, seat = 1))
                players.add(PlayerEntity(user = testPlayers[1], game = this, seat = 2))
                players.add(PlayerEntity(user = testPlayers[2], game = this, seat = 3))
            }.let {
                gameRepository.save(it)
            }

            val response = getResource<GameInfoDto>(
                path = "/v1/games/${entity.id}",
                expectedStatus = 200
            )

            assertThat(response.id).isEqualTo(entity.id)
            assertThat(response.creator.id).isEqualTo(entity.creator.id)
            assertThat(response.playerLimit).isEqualTo(entity.maxNumberOfPlayers)
            assertThat(response.players).hasSize(4)
            assertThat(response.players.map { Pair(it.user.id, it.seat) }).containsExactlyInAnyOrder(
                Pair(testUser.id, 0), Pair(testPlayers[0].id, 1), Pair(testPlayers[1].id, 2), Pair(testPlayers[2].id, 3)
            )
        }
    }

    @Nested
    inner class Creation {
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
    }


    @Nested
    inner class Starting {
        @Test
        fun `start returns 200 when model returns successful`() {
            val game = createGameOfUser(testUser).let {
                gameRepository.save(it)
            }

            mockkConstructor(GameStartModel::class)
            every { anyConstructed<GameStartModel>().start(any()) } just Runs

            val response = execPatchGame<GameInfoDto>(game.id, GameOperation.START, 200)

            assertThat(response.id).isEqualTo(game.id)
            assertThat(response.creator.id).isEqualTo(game.creator.id)
            assertThat(response.playerLimit).isEqualTo(game.maxNumberOfPlayers)
            assertThat(response.players).hasSize(game.players.size)
        }

        @Test
        fun `start returns 400 when invalid action exception`() {
            val game = createGameOfUser(testUser).let {
                gameRepository.save(it)
            }

            mockkConstructor(GameStartModel::class)
            every { anyConstructed<GameStartModel>().start(any()) } throws InvalidActionException(
                "Game:Start",
                "Mocked Model Exception."
            )

            val response = execPatchGame<ProblemDetailResponse>(game.id, GameOperation.START, 400)

            response.also {
                assertThat(response.instance.toString()).isEqualTo("/v1/games/${game.id}")
                assertThat(response.title).isEqualTo("Invalid action")
                assertThat(response.detail).isEqualTo("The action 'Game:Start' can not be performed: Mocked Model Exception.")
            }
        }

        @Test
        fun `start returns 403 when forbidden action exception`() {
            val game = createGameOfUser(testAdmin).let {
                gameRepository.save(it)
            }

            mockkConstructor(GameStartModel::class)
            every { anyConstructed<GameStartModel>().start(any()) } throws ForbiddenActionException(
                "Game:Start",
                "Mocked Model Exception."
            )

            val response = execPatchGame<ProblemDetailResponse>(game.id, GameOperation.START, 403)

            response.also {
                assertThat(response.instance.toString()).isEqualTo("/v1/games/${game.id}")
                assertThat(response.title).isEqualTo("Forbidden action")
                assertThat(response.detail).isEqualTo("You are not allowed to perform the action 'Game:Start': Mocked Model Exception.")
            }
        }
    }

    private fun createGameOfUser(userEntity: UserEntity): GameEntity {
        return GameEntity(
            creator = userEntity, maxNumberOfPlayers = 4
        ).apply {
            players.add(
                PlayerEntity(
                    user = userEntity, game = this, seat = 0
                )
            )
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