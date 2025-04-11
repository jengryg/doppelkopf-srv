package game.doppelkopf.api.play

import game.doppelkopf.BaseRestAssuredTest
import game.doppelkopf.api.play.dto.RoundInfoDto
import game.doppelkopf.api.play.dto.RoundOperationDto
import game.doppelkopf.core.common.errors.ForbiddenActionException
import game.doppelkopf.core.common.errors.InvalidActionException
import game.doppelkopf.core.common.errors.ofInvalidAction
import game.doppelkopf.core.game.model.GameModelFactory
import game.doppelkopf.core.common.enums.RoundOperation
import game.doppelkopf.core.common.enums.RoundState
import game.doppelkopf.core.play.model.RoundModelFactory
import game.doppelkopf.core.play.processor.BiddingProcessor
import game.doppelkopf.core.play.processor.DeclarationProcessor
import game.doppelkopf.errors.ProblemDetailResponse
import game.doppelkopf.persistence.model.game.GameEntity
import game.doppelkopf.persistence.model.game.GameRepository
import game.doppelkopf.persistence.model.player.PlayerEntity
import game.doppelkopf.persistence.model.player.PlayerRepository
import game.doppelkopf.persistence.model.hand.HandEntity
import game.doppelkopf.persistence.model.hand.HandRepository
import game.doppelkopf.persistence.model.round.RoundEntity
import game.doppelkopf.persistence.model.round.RoundRepository
import game.doppelkopf.persistence.model.user.UserEntity
import game.doppelkopf.utils.Quadruple
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.convention.TestBean
import java.util.*

class RoundControllerTest : BaseRestAssuredTest() {
    @Autowired
    private lateinit var gameRepository: GameRepository

    @Autowired
    private lateinit var playerRepository: PlayerRepository

    @Autowired
    private lateinit var roundRepository: RoundRepository

    @Autowired
    private lateinit var handRepository: HandRepository

    @TestBean
    private lateinit var gameModelFactory: GameModelFactory

    @TestBean
    private lateinit var roundModelFactory: RoundModelFactory

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun gameModelFactory(): GameModelFactory {
            return mockk<GameModelFactory>()
        }

        @Suppress("unused")
        @JvmStatic
        fun roundModelFactory(): RoundModelFactory {
            return mockk<RoundModelFactory>()
        }
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
            val hands = players.map {
                HandEntity(
                    round = round,
                    player = it,
                    cardsRemaining = mutableListOf(),
                    hasMarriage = false
                )
            }

            every { gameModelFactory.create(any()) } returns mockk {
                every { dealNextRound(testUser) } returns round
                every { getFourPlayersBehind(players.first) } returns players
            }
            every { roundModelFactory.create(round) } returns mockk {
                every { createPlayerHands(players) } returns hands
            }

            val (response, location) = execDealCards<RoundInfoDto>(game.id, 201)

            response.also {
                assertThat(it.gameId).isEqualTo(game.id)
                assertThat(it.dealer.id).isEqualTo(players.first.id)
                assertThat(it.dealer.user.id).isEqualTo(testUser.id)
                assertThat(it.state).isEqualTo(RoundState.INITIALIZED)
                assertThat(it.number).isEqualTo(17)
            }

            assertThat(location).isEqualTo("/v1/rounds/${round.id}")
        }

        @Test
        fun `create returns 400 when invalid action exception`() {
            every { gameModelFactory.create(any()) } returns mockk {
                every { dealNextRound(any()) } throws InvalidActionException(
                    "Round:Create",
                    "Mocked Model Exception."
                )
            }

            val game = createGameEntity(testUser).let {
                gameRepository.save(it)
            }

            val (response, location) = execDealCards<ProblemDetailResponse>(game.id, 400)

            assertThat(response.instance.toString()).isEqualTo("/v1/games/${game.id}/rounds")
            assertThat(response.title).isEqualTo("Invalid action")
            assertThat(response.detail).isEqualTo("The action 'Round:Create' can not be performed: Mocked Model Exception.")

            assertThat(location).isNull()
        }

        @Test
        fun `create returns 403 when forbidden action exception`() {
            every { gameModelFactory.create(any()) } returns mockk {
                every { dealNextRound(any()) } throws ForbiddenActionException(
                    "Round:Create",
                    "Mocked Model Exception."
                )
            }

            val game = createGameEntity(testUser).let {
                gameRepository.save(it)
            }

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
            mockkObject(DeclarationProcessor.Companion)
            val processor = mockk<DeclarationProcessor> { every { process() } just Runs }
            every {
                DeclarationProcessor.Companion.createWhenReady(any())
            } returns Result.success(processor)

            val game = createGameEntity(testUser).let { gameRepository.save(it) }
            val round = createRoundEntity(game).let { roundRepository.save(it) }

            val response = execPatchRound<RoundInfoDto>(round.id, RoundOperation.DECLARE_EVALUATION, 200)

            assertThat(response.id).isEqualTo(round.id)

            verify(exactly = 1) { DeclarationProcessor.Companion.createWhenReady(round) }
            verify(exactly = 1) { processor.process() }
            unmockkObject(DeclarationProcessor.Companion)
        }

        @Test
        fun `eval declaration returns 400 when invalid action exception`() {
            mockkObject(DeclarationProcessor.Companion)
            every { DeclarationProcessor.Companion.createWhenReady(any()) } returns Result.ofInvalidAction(
                "Declaration:Process",
                "Mocked Model Exception."
            )

            val game = createGameEntity(testUser).let { gameRepository.save(it) }
            val round = createRoundEntity(game).let { roundRepository.save(it) }

            val response = execPatchRound<ProblemDetailResponse>(round.id, RoundOperation.DECLARE_EVALUATION, 400)

            response.also {
                assertThat(response.instance.toString()).isEqualTo("/v1/rounds/${round.id}")
                assertThat(response.title).isEqualTo("Invalid action")
                assertThat(response.detail).isEqualTo("The action 'Declaration:Process' can not be performed: Mocked Model Exception.")
            }

            verify(exactly = 1) { DeclarationProcessor.Companion.createWhenReady(round) }
            unmockkObject(DeclarationProcessor.Companion)
        }
    }

    @Nested
    inner class EvalBids {
        @Test
        fun `eval bids returns 200 when processor successful`() {
            mockkObject(BiddingProcessor)
            val processor = mockk<BiddingProcessor> { every { process() } just Runs }
            every {
                BiddingProcessor.Companion.createWhenReady(any())
            } returns Result.success(processor)

            val game = createGameEntity(testUser).let { gameRepository.save(it) }
            val round = createRoundEntity(game).let { roundRepository.save(it) }

            val response = execPatchRound<RoundInfoDto>(round.id, RoundOperation.BID_EVALUATION, 200)

            assertThat(response.id).isEqualTo(round.id)

            verify(exactly = 1) { BiddingProcessor.Companion.createWhenReady(round) }
            verify(exactly = 1) { processor.process() }
            unmockkObject(BiddingProcessor)
        }

        @Test
        fun `eval bids returns 400 when invalid action exception`() {
            mockkObject(BiddingProcessor)
            every { BiddingProcessor.Companion.createWhenReady(any()) } returns Result.ofInvalidAction(
                "Bidding:Process",
                "Mocked Model Exception."
            )

            val game = createGameEntity(testUser).let { gameRepository.save(it) }
            val round = createRoundEntity(game).let { roundRepository.save(it) }

            val response = execPatchRound<ProblemDetailResponse>(round.id, RoundOperation.BID_EVALUATION, 400)

            response.also {
                assertThat(response.instance.toString()).isEqualTo("/v1/rounds/${round.id}")
                assertThat(response.title).isEqualTo("Invalid action")
                assertThat(response.detail).isEqualTo("The action 'Bidding:Process' can not be performed: Mocked Model Exception.")
            }

            verify { BiddingProcessor.Companion.createWhenReady(round) }
            unmockkObject(BiddingProcessor)
        }
    }

    private fun createRoundEntity(gameEntity: GameEntity): RoundEntity {
        return RoundEntity(
            game = gameEntity,
            dealer = gameEntity.getPlayerOfOrNull(gameEntity.creator)!!,
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

    @AfterAll
    fun `clear database`() {
        handRepository.deleteAll()
        roundRepository.deleteAll()
        playerRepository.deleteAll()
        gameRepository.deleteAll()
    }
}