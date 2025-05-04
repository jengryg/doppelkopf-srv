package game.doppelkopf.adapter.api.core

import game.doppelkopf.BaseRestAssuredTest
import game.doppelkopf.adapter.api.core.player.dto.PlayerCreateDto
import game.doppelkopf.adapter.api.core.player.dto.PlayerInfoDto
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.game.GameRepository
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.core.errors.ForbiddenActionException
import game.doppelkopf.core.errors.InvalidActionException
import game.doppelkopf.domain.game.service.GameJoinModel
import game.doppelkopf.errors.ProblemDetailResponse
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

class PlayerControllerTest : BaseRestAssuredTest() {
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
        fun `get players of game returns 200 and list of dto`() {
            val game = GameEntity(
                creator = testPlayers[0], maxNumberOfPlayers = 8
            ).also { game ->
                testPlayers.mapIndexed { index, userEntity ->
                    game.players.add(PlayerEntity(user = userEntity, game = game, seat = index))
                }
            }.let {
                gameRepository.save(it)
            }

            val response = getResourceList<PlayerInfoDto>("/v1/games/${game.id}/players", 200)

            assertThat(response).hasSize(8)

            assertThat(response.map { it.id }).containsExactlyInAnyOrderElementsOf(game.players.map { it.id })
        }

        @Test
        fun `get players of game with unknown id returns 404`() {
            val response = getResource<ProblemDetailResponse>("/v1/games/$zeroId/players", 404)

            assertThat(response.instance.toString()).isEqualTo("/v1/games/$zeroId/players")
            assertThat(response.title).isEqualTo("Entity not found")
            assertThat(response.detail).isEqualTo("The entity of type GameEntity with id $zeroId was not found.")
        }

        @Test
        fun `get specific by its id returns 200 and dto`() {
            val entity = createGameOfUser(testUser).let {
                gameRepository.save(it)
            }

            val player = entity.players.first()

            val response = getResource<PlayerInfoDto>("/v1/players/${player.id}", 200)

            response.also {
                assertThat(it.id).isEqualTo(player.id)
                assertThat(it.gameId).isEqualTo(entity.id)
                assertThat(it.seat).isEqualTo(player.seat)
                assertThat(it.dealer).isFalse()
                assertThat(it.user.id).isEqualTo(player.user.id)
            }
        }
    }

    @Nested
    inner class Join {
        @Test
        fun `join returns 400 when invalid action exception`() {
            val game = createGameOfUser(testAdmin).let {
                gameRepository.save(it)
            }

            mockkConstructor(GameJoinModel::class)
            every { anyConstructed<GameJoinModel>().join(any(), 3) } throws InvalidActionException(
                "Game:Join",
                "Mocked Model Exception."
            )

            val (response, location) = execJoinGame<ProblemDetailResponse>(game.id, 3, 400)

            assertThat(response.instance.toString()).isEqualTo("/v1/games/${game.id}/players")
            assertThat(response.title).isEqualTo("Invalid action")
            assertThat(response.detail).isEqualTo("The action 'Game:Join' can not be performed: Mocked Model Exception.")

            assertThat(location).isNull()
        }

        @Test
        fun `join returns 403 when forbidden action exception`() {
            val game = createGameOfUser(testAdmin).let {
                gameRepository.save(it)
            }

            mockkConstructor(GameJoinModel::class)
            every { anyConstructed<GameJoinModel>().join(any(), 3) } throws ForbiddenActionException(
                "Game:Join",
                "Mocked Model Exception."
            )

            val (response, location) = execJoinGame<ProblemDetailResponse>(game.id, 3, 403)

            assertThat(response.instance.toString()).isEqualTo("/v1/games/${game.id}/players")
            assertThat(response.title).isEqualTo("Forbidden action")
            assertThat(response.detail).isEqualTo("You are not allowed to perform the action 'Game:Join': Mocked Model Exception.")

            assertThat(location).isNull()
        }

        @ParameterizedTest
        @ValueSource(ints = [1, 2, 3, 4, 5, 6, 7])
        fun `join game at valid seat position returns 201 and dto`(seat: Int) {
            val game = gameRepository.save(createGameOfUser(testAdmin))
            val player = PlayerEntity(user = testUser, game = game, seat = seat)

            mockkConstructor(GameJoinModel::class)
            every { anyConstructed<GameJoinModel>().join(any(), seat) } returns mockk {
                every { entity } returns player
            }

            val (response, location) = execJoinGame<PlayerInfoDto>(game.id, seat, 201)

            response.also {
                assertThat(it.id).isEqualTo(player.id)
                assertThat(it.user.id).isEqualTo(player.user.id)
                assertThat(it.seat).isEqualTo(player.seat)
                assertThat(it.gameId).isEqualTo(player.game.id)
            }

            assertThat(location).isEqualTo("/v1/players/${player.id}")
        }

        @ParameterizedTest
        @ValueSource(ints = [0, 8, -17, 42])
        fun `join game at invalid seat position returns 400 bad request`(seat: Int) {
            val game = gameRepository.save(createGameOfUser(testAdmin))

            val (response, location) = execJoinGame<ProblemDetailResponse>(game.id, seat, 400)

            assertThat(response.instance.toString()).isEqualTo("/v1/games/${game.id}/players")
            assertThat(response.title).isEqualTo("Bad Request")
            assertThat(response.detail).isEqualTo("Invalid request content.")

            assertThat(location).isNull()
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

    private final inline fun <reified T> execJoinGame(
        gameId: UUID,
        seat: Int,
        expectedStatus: Int,
    ): Pair<T, String?> {
        return createResource<PlayerCreateDto, T>(
            path = "/v1/games/$gameId/players", body = PlayerCreateDto(
                seat = seat
            ), expectedStatus = expectedStatus
        )
    }
}