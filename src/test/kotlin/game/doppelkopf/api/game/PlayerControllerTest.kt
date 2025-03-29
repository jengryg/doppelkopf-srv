package game.doppelkopf.api.game

import game.doppelkopf.BaseRestAssuredTest
import game.doppelkopf.api.game.dto.PlayerCreateDto
import game.doppelkopf.api.game.dto.PlayerInfoDto
import game.doppelkopf.core.game.GameState
import game.doppelkopf.errors.ProblemDetailResponse
import game.doppelkopf.persistence.game.GameEntity
import game.doppelkopf.persistence.game.GameRepository
import game.doppelkopf.persistence.game.PlayerEntity
import game.doppelkopf.persistence.user.UserEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class PlayerControllerTest : BaseRestAssuredTest() {
    @Autowired
    private lateinit var gameRepository: GameRepository

    @Test
    fun `get players of game returns 200 and list of dto`() {
        val game = GameEntity(
            creator = testPlayers[0],
            maxNumberOfPlayers = 8
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
    fun `joining game second time returns 400`() {
        val game = createGameOfUser(testAdmin).apply {
            players.add(PlayerEntity(user = testUser, game = this, seat = 4))
            // testUser is already in the game
        }.let { gameRepository.save(it) }

        val (response, location) = execJoinGame<ProblemDetailResponse>(game.id, 3, 400)

        assertThat(response.instance.toString()).isEqualTo("/v1/games/${game.id}/players")
        assertThat(response.title).isEqualTo("Invalid action")
        assertThat(response.detail).isEqualTo("The action 'Game:Join' can not be performed: You already joined this game.")

        assertThat(location).isNull()
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3, 4, 5, 6, 7])
    fun `join game at valid seat position returns 201 and dto`(seat: Int) {
        val game = gameRepository.save(createGameOfUser(testAdmin))

        val (response, location) = execJoinGame<PlayerInfoDto>(game.id, seat, 201)

        response.also {
            assertThat(it.user.id).isEqualTo(testUser.id)
            assertThat(it.seat).isEqualTo(seat)
            assertThat(it.gameId).isEqualTo(game.id)
        }

        assertThat(location).isNotNull()

        getResource<PlayerInfoDto>(location!!, 200).also {
            // verify that the location header points to the correct resource
            assertThat(it.user.id).isEqualTo(testUser.id)
            assertThat(it.seat).isEqualTo(seat)
            assertThat(it.gameId).isEqualTo(game.id)
        }
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

    @Test
    fun `joining a full table returns 400 bad request`() {
        val game = gameRepository.save(
            createGameOfUser(testPlayers[3]).apply {
                repeat(3) {
                    players.add(PlayerEntity(user = testPlayers[it], game = this, seat = it + 1))
                }
            }
        )

        val (response, location) = execJoinGame<ProblemDetailResponse>(game.id, 7, 400)

        assertThat(response.instance.toString()).isEqualTo("/v1/games/${game.id}/players")
        assertThat(response.title).isEqualTo("Invalid action")
        assertThat(response.detail).isEqualTo("The action 'Game:Join' can not be performed: This game is already at its maximum capacity.")

        assertThat(location).isNull()
    }

    @Test
    fun `joining at an already taken seat position returns 400 bad request`() {
        val game = gameRepository.save(
            createGameOfUser(testAdmin).apply {
                players.add(
                    PlayerEntity(
                        user = testPlayers[0],
                        game = this,
                        seat = 3
                    )
                )
            }
        )

        val (response, location) = execJoinGame<ProblemDetailResponse>(game.id, 3, 400)

        assertThat(response.instance.toString()).isEqualTo("/v1/games/${game.id}/players")
        assertThat(response.title).isEqualTo("Invalid action")
        assertThat(response.detail).isEqualTo("The action 'Game:Join' can not be performed: The seat you have chosen is already taken by another player.")

        assertThat(location).isNull()
    }

    @Test
    fun `joining an already started game returns 400 bad request`() {
        val game = gameRepository.save(
            createGameOfUser(testAdmin).apply {
                state = GameState.WAITING_FOR_DEAL
            }
        )

        val (response, location) = execJoinGame<ProblemDetailResponse>(game.id, 7, 400)

        assertThat(response.instance.toString()).isEqualTo("/v1/games/${game.id}/players")
        assertThat(response.title).isEqualTo("Invalid action")
        assertThat(response.detail).isEqualTo("The action 'Game:Join' can not be performed: You can not join a game that has already started.")

        assertThat(location).isNull()
    }

    private fun createGameOfUser(userEntity: UserEntity): GameEntity {
        return GameEntity(
            creator = userEntity,
            maxNumberOfPlayers = 4
        ).apply {
            players.add(
                PlayerEntity(
                    user = userEntity,
                    game = this,
                    seat = 0
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
            path = "/v1/games/$gameId/players",
            body = PlayerCreateDto(
                seat = seat
            ),
            expectedStatus = expectedStatus
        )
    }
}