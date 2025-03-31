package game.doppelkopf.api.play

import game.doppelkopf.BaseRestAssuredTest
import game.doppelkopf.api.play.dto.RoundInfoDto
import game.doppelkopf.core.game.enums.GameState
import game.doppelkopf.core.play.enums.RoundState
import game.doppelkopf.errors.ProblemDetailResponse
import game.doppelkopf.persistence.game.GameEntity
import game.doppelkopf.persistence.game.GameRepository
import game.doppelkopf.persistence.game.PlayerEntity
import game.doppelkopf.persistence.play.RoundEntity
import game.doppelkopf.persistence.user.UserEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class RoundControllerTest : BaseRestAssuredTest() {
    @Autowired
    private lateinit var gameRepository: GameRepository

    @Test
    fun `get rounds of game that has no rounds returns empty list`() {
        val game = gameRepository.save(createGameEntity(testUser))

        val response = getResourceList<RoundInfoDto>("/v1/games/${game.id}/rounds", 200)

        assertThat(response).isEmpty()
    }

    @Test
    fun `get rounds of game with unknown id returns 404`() {
        val response = getResource<ProblemDetailResponse>("/v1/games/$zeroId/rounds", 404)

        assertThat(response.instance.toString()).isEqualTo("/v1/games/$zeroId/rounds")
        assertThat(response.title).isEqualTo("Entity not found")
        assertThat(response.detail).isEqualTo("The entity of type GameEntity with id $zeroId was not found.")
    }

    @Test
    fun `first round creation for game in valid case returns 201 and dto`() {
        val game = gameRepository.save(
            createGameEntity(testUser).apply {
                state = GameState.WAITING_FOR_DEAL
                getPlayerOfOrNull(testUser)!!.dealer = true
            }
        )

        val (response, location) = execDealCards<RoundInfoDto>(game.id, 201)

        response.also {
            assertThat(it.gameId).isEqualTo(game.id)
            assertThat(it.dealer.id).isEqualTo(game.getPlayerOfOrNull(testUser)!!.id)
            assertThat(it.dealer.user.id).isEqualTo(testUser.id)
            assertThat(it.state).isEqualTo(RoundState.INITIALIZED)
            assertThat(it.number).isEqualTo(1)
        }

        assertThat(location).isNotNull()

        getResource<RoundInfoDto>(location!!, 200).also {
            assertThat(it.gameId).isEqualTo(game.id)
            assertThat(it.dealer.id).isEqualTo(game.getPlayerOfOrNull(testUser)!!.id)
            assertThat(it.dealer.user.id).isEqualTo(testUser.id)
            assertThat(it.state).isEqualTo(RoundState.INITIALIZED)
            assertThat(it.number).isEqualTo(1)
        }
    }

    @Test
    fun `additional round creation for game in valid case returns 201 and dto`() {
        val game = gameRepository.save(
            createGameEntity(testUser).apply {
                state = GameState.WAITING_FOR_DEAL
                players.forEachIndexed { idx, player ->
                    rounds.add(RoundEntity(game = this, dealer = player, number = idx + 1))
                }
                getPlayerOfOrNull(testUser)!!.dealer = true
            }
        )

        val (response, location) = execDealCards<RoundInfoDto>(game.id, 201)

        response.also {
            assertThat(it.gameId).isEqualTo(game.id)
            assertThat(it.dealer.id).isEqualTo(game.getPlayerOfOrNull(testUser)!!.id)
            assertThat(it.dealer.user.id).isEqualTo(testUser.id)
            assertThat(it.state).isEqualTo(RoundState.INITIALIZED)
            assertThat(it.number).isEqualTo(5)
        }

        assertThat(location).isNotNull()

        getResource<RoundInfoDto>(location!!, 200).also {
            assertThat(it.gameId).isEqualTo(game.id)
            assertThat(it.dealer.id).isEqualTo(game.getPlayerOfOrNull(testUser)!!.id)
            assertThat(it.dealer.user.id).isEqualTo(testUser.id)
            assertThat(it.state).isEqualTo(RoundState.INITIALIZED)
            assertThat(it.number).isEqualTo(5)
        }
    }

    @ParameterizedTest
    @EnumSource(value = GameState::class, names = ["WAITING_FOR_DEAL"], mode = EnumSource.Mode.EXCLUDE)
    fun `round creation when game is not in correct state returns 400`(gameState: GameState) {
        val game = gameRepository.save(
            createGameEntity(testUser).apply {
                state = gameState
                getPlayerOfOrNull(testUser)!!.dealer = true
            }
        )

        val (response, location) = execDealCards<ProblemDetailResponse>(game.id, 400)

        assertThat(response.instance.toString()).isEqualTo("/v1/games/${game.id}/rounds")
        assertThat(response.title).isEqualTo("Invalid action")
        assertThat(response.detail).isEqualTo("The action 'Round:Create' can not be performed: The game is currently not in the WAITING_FOR_DEAL state.")

        assertThat(location).isNull()
    }

    @Test
    fun `round creation by player that is not dealer returns 403`() {
        val game = gameRepository.save(
            createGameEntity(testUser).apply {
                state = GameState.WAITING_FOR_DEAL
                players.first { it.user != testUser }.dealer = true
                getPlayerOfOrNull(testUser)!!.dealer = false
            }
        )

        val (response, location) = execDealCards<ProblemDetailResponse>(game.id, 403)

        assertThat(response.instance.toString()).isEqualTo("/v1/games/${game.id}/rounds")
        assertThat(response.title).isEqualTo("Forbidden action")
        assertThat(response.detail).isEqualTo("You are not allowed to perform the action 'Round:Create': Only the current dealer of the game can start this round.")

        assertThat(location).isNull()
    }

    private fun createGameEntity(creator: UserEntity): GameEntity {
        return GameEntity(
            creator = creator,
            maxNumberOfPlayers = 4
        ).also { game ->
            game.players.add(PlayerEntity(user = creator, game = game, seat = 0))

            testPlayers.filter { it != creator }.take(3).mapIndexed { index, userEntity ->
                game.players.add(PlayerEntity(user = userEntity, game = game, seat = index + 1))
            }
        }
    }

    private final inline fun <reified T> execDealCards(
        gameId: UUID,
        expectedStatus: Int
    ): Pair<T, String?> {
        return createResource<T>(
            path = "/v1/games/${gameId}/rounds",
            expectedStatus = expectedStatus
        )
    }

    @AfterAll
    fun `clear database`() {
        println("AFTER ALL IS TRIGGERED")
        gameRepository.deleteAll()
    }
}