package game.doppelkopf.adapter.graphql.core.round

import game.doppelkopf.BaseGraphQLTest
import game.doppelkopf.adapter.graphql.core.round.dto.RoundResponse
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.game.GameRepository
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.adapter.persistence.model.player.PlayerRepository
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.round.RoundRepository
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.common.errors.ForbiddenActionException
import game.doppelkopf.common.errors.InvalidActionException
import game.doppelkopf.domain.game.enums.GameState
import game.doppelkopf.domain.game.service.GameDealModel
import game.doppelkopf.domain.round.enums.RoundContract
import game.doppelkopf.domain.round.enums.RoundState
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.test.tester.GraphQlTester
import java.util.*

class RoundGraphQLControllerTest : BaseGraphQLTest() {
    @Autowired
    private lateinit var gameRepository: GameRepository

    @Autowired
    private lateinit var playerRepository: PlayerRepository

    @Autowired
    private lateinit var roundRepository: RoundRepository

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
            getRound(zeroId)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Entity not found")
                }.verify()
        }

        @Test
        fun `get specific with known uuid returns data`() {
            val game = createGameEntity(testUser).apply {
                rounds.add(RoundEntity(game = this, dealer = players.first(), 1, seed = ByteArray(256)))
            }.let {
                gameRepository.save(it)
            }
            playerRepository.saveAll(game.players)
            val round = game.rounds.first()

            val response = getRound(round.id)
                .execute()
                .toSingleEntity<RoundResponse>()

            // TODO
            assertThat(response.id).isEqualTo(round.id)
            assertThat(response.state).isEqualTo(round.state)
            assertThat(response.number).isEqualTo(round.number)
            assertThat(response.contract).isEqualTo(round.contract.roundContractPublic)
            assertThat(response.cu.created).isEqualTo(round.created)
            assertThat(response.cu.updated).isEqualTo(round.updated)
            assertThat(response.se.started).isNull()
            assertThat(response.se.ended).isNull()
            assertThat(response.result).isNull()
        }
    }

    @Nested
    inner class Mutations {
        @Test
        fun `dealing cards creates round and returns data`() {
            val game = createGameEntity(testUser).apply {
                state = GameState.WAITING_FOR_DEAL
                players.single { it.user.id == testUser.id }.dealer = true
            }.let { gameRepository.save(it) }
            playerRepository.saveAll(game.players)

            val response = dealCards(game.id)
                .execute()
                .toSingleEntity<RoundResponse>()

            assertThat(response.number).isEqualTo(1)
            assertThat(response.state).isEqualTo(RoundState.WAITING_FOR_DECLARATIONS)
            assertThat(response.contract).isEqualTo(RoundContract.UNDECIDED.roundContractPublic)
        }

        @Test
        fun `dealing returns error when forbidden action exception`() {
            val game = createGameEntity(testUser).let {
                gameRepository.save(it)
            }

            mockkConstructor(GameDealModel::class)
            every { anyConstructed<GameDealModel>().deal(any()) } throws ForbiddenActionException(
                "Mocked Model Exception."
            )

            dealCards(game.id)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Forbidden action")
                }.verify()
        }

        @Test
        fun `dealing returns error when invalid action exception`() {
            val game = createGameEntity(testUser).let {
                gameRepository.save(it)
            }

            mockkConstructor(GameDealModel::class)
            every { anyConstructed<GameDealModel>().deal(any()) } throws InvalidActionException(
                "Mocked Model Exception."
            )

            dealCards(game.id)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Invalid action")
                }.verify()
        }
    }

    private fun getRound(roundId: UUID): GraphQlTester.Request<*> {
        return gqlUserTester
            .documentName("getRound")
            .fragName("roundProperties")
            .fragName("resultProperties")
            .fragName("sq")
            .fragName("cu")
            .fragName("se")
            .variable("id", roundId)
    }

    private fun dealCards(gameId: UUID): GraphQlTester.Request<*> {
        return gqlUserTester
            .documentName("createRound")
            .fragName("roundProperties")
            .fragName("resultProperties")
            .fragName("sq")
            .fragName("cu")
            .fragName("se")
            .variable("gameId", gameId)
    }

    private fun createGameEntity(creator: UserEntity): GameEntity {
        return GameEntity(
            creator = creator,
            maxNumberOfPlayers = 4,
            seed = ByteArray(256)
        ).also { game ->
            game.players.add(PlayerEntity(user = creator, game = game, seat = 0))

            testPlayers.filter { it != creator }.take(3).mapIndexed { index, userEntity ->
                game.players.add(PlayerEntity(user = userEntity, game = game, seat = index + 1))
            }
        }
    }
}