package game.doppelkopf.adapter.api.core

import game.doppelkopf.BaseRestAssuredTest
import game.doppelkopf.adapter.api.core.round.dto.RoundInfoDto
import game.doppelkopf.adapter.api.core.round.dto.RoundOperationDto
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.game.GameRepository
import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.adapter.persistence.model.player.PlayerRepository
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.round.RoundRepository
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.domain.round.enums.RoundOperation
import game.doppelkopf.domain.round.enums.RoundState
import game.doppelkopf.core.errors.ForbiddenActionException
import game.doppelkopf.core.errors.InvalidActionException
import game.doppelkopf.domain.game.service.GameDealModel
import game.doppelkopf.domain.round.service.RoundBidsEvaluationModel
import game.doppelkopf.domain.round.service.RoundDeclarationEvaluationModel
import game.doppelkopf.domain.round.service.RoundMarriageResolverModel
import game.doppelkopf.errors.ProblemDetailResponse
import game.doppelkopf.utils.Quadruple
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class RoundControllerTest : BaseRestAssuredTest() {
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
    inner class GetAndList {
        @Test
        fun `get rounds of game that has no rounds returns empty list`() {
            val game = createGameEntity(testUser).let {
                gameRepository.save(it)
            }
            playerRepository.saveAll(game.players)

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
        fun `get rounds of game that has some rounds returns 200 with list of dto`() {
            val game = createGameEntity(testUser).apply {
                rounds.add(RoundEntity(game = this, dealer = players.first(), 1))
                rounds.add(RoundEntity(game = this, dealer = players.first(), 2))
                rounds.add(RoundEntity(game = this, dealer = players.first(), 3))
                rounds.add(RoundEntity(game = this, dealer = players.first(), 4))
            }.let {
                gameRepository.save(it)
            }
            playerRepository.saveAll(game.players)

            val response = getResourceList<RoundInfoDto>("/v1/games/${game.id}/rounds", 200)

            assertThat(response).hasSize(4)
            assertThat(response.map { it.number }).containsExactlyInAnyOrder(1, 2, 3, 4)
            assertThat(response.map { it.id }).containsExactlyInAnyOrderElementsOf(game.rounds.map { it.id })

            response.forEach {
                assertThat(it.gameId).isEqualTo(game.id)
                assertThat(it.dealer.id).isEqualTo(game.players.first().id)
            }
        }

        @Test
        fun `get specific round with unknown uuid returns 404`() {
            val response = getResource<ProblemDetailResponse>(
                path = "/v1/rounds/$zeroId",
                expectedStatus = 404
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/rounds/$zeroId")
            assertThat(response.title).isEqualTo("Entity not found")
            assertThat(response.detail).isEqualTo("The entity of type RoundEntity with id $zeroId was not found.")
        }

        @Test
        fun `get specific round with known uuid returns 200 and dto`() {
            val game = createGameEntity(testUser).apply {
                rounds.add(RoundEntity(game = this, dealer = players.first(), 1))
            }.let {
                gameRepository.save(it)
            }
            playerRepository.saveAll(game.players)
            val round = game.rounds.first()

            val response = getResource<RoundInfoDto>(
                path = "/v1/rounds/${round.id}",
                expectedStatus = 200
            )

            assertThat(response.id).isEqualTo(round.id)
            assertThat(response.number).isEqualTo(round.number)
            assertThat(response.dealer.id).isEqualTo(round.dealer.id)
            assertThat(response.gameId).isEqualTo(game.id)
            assertThat(response.state).isEqualTo(round.state)
        }
    }

    @Nested
    inner class Create {
        @Test
        fun `round creation in valid case returns 201 and dto`() {
            val game = createGameEntity(testUser).let {
                gameRepository.save(it)
            }
            val players = Quadruple(
                PlayerEntity(game = game, user = testUser, seat = 4),
                PlayerEntity(game = game, user = testPlayers[0], seat = 5),
                PlayerEntity(game = game, user = testPlayers[1], seat = 6),
                PlayerEntity(game = game, user = testPlayers[2], seat = 4)
            ).also { playerRepository.saveAll(it.toList()) }
            val round = RoundEntity(game = game, dealer = players.first, number = 17)
            val hands = players.mapIndexed { index, it ->
                HandEntity(
                    round = round,
                    player = it,
                    index = index,
                    cardsRemaining = mutableListOf(),
                    hasMarriage = false
                )
            }

            mockkConstructor(GameDealModel::class)
            every { anyConstructed<GameDealModel>().deal(any()) } returns Pair(
                first = mockk { every { entity } returns round },
                second = hands.map { mockk { every { entity } returns it } }
            )

            val (response, location) = execDealCards<RoundInfoDto>(game.id, 201)

            response.also {
                assertThat(it.gameId).isEqualTo(game.id)
                assertThat(it.dealer.id).isEqualTo(players.first.id)
                assertThat(it.dealer.user.id).isEqualTo(testUser.id)
                assertThat(it.state).isEqualTo(RoundState.WAITING_FOR_DECLARATIONS)
                assertThat(it.number).isEqualTo(17)
            }

            assertThat(location).isEqualTo("/v1/rounds/${round.id}")
        }

        @Test
        fun `create returns 400 when invalid action exception`() {
            val game = createGameEntity(testUser).let {
                gameRepository.save(it)
            }

            mockkConstructor(GameDealModel::class)
            every { anyConstructed<GameDealModel>().deal(any()) } throws InvalidActionException(
                "Round:Create",
                "Mocked Model Exception."
            )

            val (response, location) = execDealCards<ProblemDetailResponse>(game.id, 400)

            assertThat(response.instance.toString()).isEqualTo("/v1/games/${game.id}/rounds")
            assertThat(response.title).isEqualTo("Invalid action")
            assertThat(response.detail).isEqualTo("The action 'Round:Create' can not be performed: Mocked Model Exception.")

            assertThat(location).isNull()
        }

        @Test
        fun `create returns 403 when forbidden action exception`() {
            val game = createGameEntity(testUser).let {
                gameRepository.save(it)
            }

            mockkConstructor(GameDealModel::class)
            every { anyConstructed<GameDealModel>().deal(any()) } throws ForbiddenActionException(
                "Round:Create",
                "Mocked Model Exception."
            )

            val (response, location) = execDealCards<ProblemDetailResponse>(game.id, 403)

            assertThat(response.instance.toString()).isEqualTo("/v1/games/${game.id}/rounds")
            assertThat(response.title).isEqualTo("Forbidden action")
            assertThat(response.detail).isEqualTo("You are not allowed to perform the action 'Round:Create': Mocked Model Exception.")

            assertThat(location).isNull()
        }
    }

    @Nested
    inner class EvalDeclarations {
        @Test
        fun `eval declaration returns 200 when processor successful`() {
            val game = createGameEntity(testUser).let { gameRepository.save(it) }
            val round = createRoundEntity(game).let { roundRepository.save(it) }

            mockkConstructor(RoundDeclarationEvaluationModel::class)
            every { anyConstructed<RoundDeclarationEvaluationModel>().evaluateDeclarations() } just Runs

            val response = execPatchRound<RoundInfoDto>(round.id, RoundOperation.DECLARE_EVALUATION, 200)

            assertThat(response.id).isEqualTo(round.id)
        }

        @Test
        fun `eval declaration returns 400 when invalid action exception`() {
            val game = createGameEntity(testUser).let { gameRepository.save(it) }
            val round = createRoundEntity(game).let { roundRepository.save(it) }

            mockkConstructor(RoundDeclarationEvaluationModel::class)
            every { anyConstructed<RoundDeclarationEvaluationModel>().evaluateDeclarations() } throws InvalidActionException(
                "Declaration:Process",
                "Mocked Model Exception."
            )

            val response = execPatchRound<ProblemDetailResponse>(round.id, RoundOperation.DECLARE_EVALUATION, 400)

            response.also {
                assertThat(response.instance.toString()).isEqualTo("/v1/rounds/${round.id}")
                assertThat(response.title).isEqualTo("Invalid action")
                assertThat(response.detail).isEqualTo("The action 'Declaration:Process' can not be performed: Mocked Model Exception.")
            }
        }
    }

    @Nested
    inner class EvalBids {
        @Test
        fun `eval bids returns 200 when processor successful`() {
            val game = createGameEntity(testUser).let { gameRepository.save(it) }
            val round = createRoundEntity(game).let { roundRepository.save(it) }

            mockkConstructor(RoundBidsEvaluationModel::class)
            every { anyConstructed<RoundBidsEvaluationModel>().evaluateBids() } just Runs

            val response = execPatchRound<RoundInfoDto>(round.id, RoundOperation.BID_EVALUATION, 200)

            assertThat(response.id).isEqualTo(round.id)
        }

        @Test
        fun `eval bids returns 400 when invalid action exception`() {
            val game = createGameEntity(testUser).let { gameRepository.save(it) }
            val round = createRoundEntity(game).let { roundRepository.save(it) }

            mockkConstructor(RoundBidsEvaluationModel::class)
            every { anyConstructed<RoundBidsEvaluationModel>().evaluateBids() } throws InvalidActionException(
                "Bidding:Process",
                "Mocked Model Exception."
            )

            val response = execPatchRound<ProblemDetailResponse>(round.id, RoundOperation.BID_EVALUATION, 400)

            response.also {
                assertThat(response.instance.toString()).isEqualTo("/v1/rounds/${round.id}")
                assertThat(response.title).isEqualTo("Invalid action")
                assertThat(response.detail).isEqualTo("The action 'Bidding:Process' can not be performed: Mocked Model Exception.")
            }
        }
    }

    @Nested
    inner class ResolveMarriage {
        @Test
        fun `resolve marriage returns 200 when processor successful`() {
            val game = createGameEntity(testUser).let { gameRepository.save(it) }
            val round = createRoundEntity(game).let { roundRepository.save(it) }

            mockkConstructor(RoundMarriageResolverModel::class)
            every { anyConstructed<RoundMarriageResolverModel>().resolveMarriage() } just Runs

            val response = execPatchRound<RoundInfoDto>(round.id, RoundOperation.MARRIAGE_RESOLVER, 200)

            assertThat(response.id).isEqualTo(round.id)
        }

        @Test
        fun `resolve marriage returns 400 when invalid action exception`() {
            val game = createGameEntity(testUser).let { gameRepository.save(it) }
            val round = createRoundEntity(game).let { roundRepository.save(it) }

            mockkConstructor(RoundMarriageResolverModel::class)
            every { anyConstructed<RoundMarriageResolverModel>().resolveMarriage() } throws InvalidActionException(
                "Marriage:Resolve",
                "Mocked Model Exception."
            )

            val response = execPatchRound<ProblemDetailResponse>(round.id, RoundOperation.MARRIAGE_RESOLVER, 400)

            response.also {
                assertThat(response.instance.toString()).isEqualTo("/v1/rounds/${round.id}")
                assertThat(response.title).isEqualTo("Invalid action")
                assertThat(response.detail).isEqualTo("The action 'Marriage:Resolve' can not be performed: Mocked Model Exception.")
            }
        }
    }

    private fun createRoundEntity(gameEntity: GameEntity): RoundEntity {
        return RoundEntity(
            game = gameEntity,
            dealer = gameEntity.players.single { it.user.id == gameEntity.creator.id },
            number = 1
        )
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

    private final inline fun <reified T> execPatchRound(
        roundId: UUID,
        operation: RoundOperation,
        expectedStatus: Int
    ): T {
        return patchResource<RoundOperationDto, T>(
            path = "/v1/rounds/$roundId",
            body = RoundOperationDto(
                op = operation
            ),
            expectedStatus = expectedStatus
        )
    }
}