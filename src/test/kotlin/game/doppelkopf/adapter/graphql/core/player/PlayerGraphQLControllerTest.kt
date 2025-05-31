package game.doppelkopf.adapter.graphql.core.player

import game.doppelkopf.BaseGraphQLTest
import game.doppelkopf.adapter.graphql.core.player.dto.PlayerResponse
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.game.GameRepository
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.common.errors.ForbiddenActionException
import game.doppelkopf.common.errors.InvalidActionException
import game.doppelkopf.domain.game.service.GameJoinModel
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

class PlayerGraphQLControllerTest : BaseGraphQLTest() {
    @Autowired
    private lateinit var gameRepository: GameRepository

    @BeforeEach
    @AfterEach
    fun `unmock all`() {
        clearAllMocks()
        unmockkAll()
    }

    @Nested
    inner class Queries {
        @Test
        fun `get specific with unknown uuid returns error`() {
            getPlayer(zeroId)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Entity not found")
                }.verify()
        }

        @Test
        fun `get specified with known uuid returns player`() {
            val entity = createGameOfUser(testUser).let {
                gameRepository.save(it)
            }

            val player = entity.players.first()

            val response = getPlayer(player.id)
                .execute()
                .toSingleEntity<PlayerResponse>()

            assertThat(response.id).isEqualTo(player.id)
            assertThat(response.seat).isEqualTo(player.seat)
            assertThat(response.dealer).isEqualTo(player.dealer)
            assertThat(response.cu.created).isEqualTo(player.created)
            assertThat(response.cu.updated).isEqualTo(player.updated)
        }
    }

    @Nested
    inner class Mutations {
        @ParameterizedTest
        @ValueSource(ints = [1, 2, 3, 4, 5, 6, 7])
        fun `join game at valid seat position creates player and returns data`(seat: Int) {
            val game = gameRepository.save(createGameOfUser(testAdmin))

            val response = joinGame(game.id, seat)
                .execute()
                .toSingleEntity<PlayerResponse>()

            assertThat(response.seat).isEqualTo(seat)
        }

        @ParameterizedTest
        @ValueSource(ints = [0, 8, -17, 42])
        fun `join game at invalid position returns error`(seat: Int) {
            val game = gameRepository.save(createGameOfUser(testAdmin))

            joinGame(game.id, seat)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Validation failed")
                }.verify()
        }

        @Test
        fun `join returns error when forbidden action exception`() {
            val game = gameRepository.save(createGameOfUser(testAdmin))

            mockkConstructor(GameJoinModel::class)
            every { anyConstructed<GameJoinModel>().join(any(), 3) } throws ForbiddenActionException(
                "Mocked Model Exception."
            )

            joinGame(game.id, 3)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Forbidden action")
                }.verify()
        }

        @Test
        fun `join returns error when invalid action exception`() {
            val game = gameRepository.save(createGameOfUser(testAdmin))

            mockkConstructor(GameJoinModel::class)
            every { anyConstructed<GameJoinModel>().join(any(), 3) } throws InvalidActionException(
                "Mocked Model Exception."
            )

            joinGame(game.id, 3)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Invalid action")
                }.verify()
        }
    }

    private fun getPlayer(playerId: UUID): GraphQlTester.Request<*> {
        return gqlUserTester
            .documentName("getPlayer")
            .fragName("playerProperties")
            .fragName("cu")
            .variable("id", playerId)
    }

    private fun joinGame(gameId: UUID, seat: Int): GraphQlTester.Request<*> {
        return gqlUserTester
            .documentName("joinGame")
            .fragName("playerProperties")
            .fragName("cu")
            .variable("gameId", gameId)
            .variable("seat", seat)
    }

    private fun createGameOfUser(userEntity: UserEntity): GameEntity {
        return GameEntity(
            creator = userEntity, maxNumberOfPlayers = 4, seed = ByteArray(256)
        ).apply {
            players.add(
                PlayerEntity(
                    user = userEntity, game = this, seat = 0
                )
            )
        }
    }
}