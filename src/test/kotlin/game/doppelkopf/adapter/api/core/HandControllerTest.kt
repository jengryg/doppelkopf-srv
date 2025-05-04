package game.doppelkopf.adapter.api.core

import game.doppelkopf.BaseRestAssuredTest
import game.doppelkopf.adapter.api.core.hand.dto.BidCreateDto
import game.doppelkopf.adapter.api.core.hand.dto.DeclarationCreateDto
import game.doppelkopf.adapter.api.core.hand.dto.HandForPlayerDto
import game.doppelkopf.adapter.api.core.hand.dto.HandPublicInfoDto
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.game.GameRepository
import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.adapter.persistence.model.hand.HandRepository
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.adapter.persistence.model.player.PlayerRepository
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.round.RoundRepository
import game.doppelkopf.core.errors.ForbiddenActionException
import game.doppelkopf.core.errors.InvalidActionException
import game.doppelkopf.domain.hand.enums.*
import game.doppelkopf.domain.hand.service.HandBiddingModel
import game.doppelkopf.domain.hand.service.HandDeclareModel
import game.doppelkopf.domain.round.service.RoundBidsEvaluationModel
import game.doppelkopf.domain.round.service.RoundDeclarationEvaluationModel
import game.doppelkopf.errors.ProblemDetailResponse
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class HandControllerTest : BaseRestAssuredTest() {
    @Autowired
    private lateinit var gameRepository: GameRepository

    @Autowired
    private lateinit var playerRepository: PlayerRepository

    @Autowired
    private lateinit var roundRepository: RoundRepository

    @Autowired
    private lateinit var handRepository: HandRepository

    @BeforeEach
    @AfterEach
    fun `unmock all`() {
        clearAllMocks()
        unmockkAll()
    }

    @Nested
    inner class GetAndListPublic {
        @Test
        fun `get hands of round with unknown id returns 404`() {
            val response = getResource<ProblemDetailResponse>(
                path = "/v1/rounds/$zeroId/hands",
                expectedStatus = 404
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/rounds/$zeroId/hands")
            assertThat(response.title).isEqualTo("Entity not found")
            assertThat(response.detail).isEqualTo("The entity of type RoundEntity with id $zeroId was not found.")
        }

        @Test
        fun `get hands of round that has hands returns 200 and list of dto`() {
            val round = createPersistedRoundEntity()

            val response = getResourceList<HandPublicInfoDto>(
                path = "/v1/rounds/${round.id}/hands",
                expectedStatus = 200
            )

            assertThat(response).hasSize(4)
            assertThat(response.map { it.id }).containsExactlyInAnyOrderElementsOf(round.hands.map { it.id })
            assertThat(response.map { it.playerId }).containsExactlyInAnyOrderElementsOf(round.hands.map { it.player.id })

            response.forEach {
                assertThat(it.roundId).isEqualTo(round.id)
                assertThat(it.publicTeam).isEqualTo(Team.NA)
                assertThat(it.bid).isEqualTo(Bidding.NOTHING)
                assertThat(it.declared).isEqualTo(DeclarationPublic.NOTHING)
            }
        }
    }

    @Nested
    inner class GetHandOfPlayerOnly {
        @Test
        fun `get specific with unknown uuid returns 404`() {
            val response = getResource<ProblemDetailResponse>(
                path = "/v1/hands/$zeroId",
                expectedStatus = 404
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/hands/$zeroId")
            assertThat(response.title).isEqualTo("Entity not found")
            assertThat(response.detail).isEqualTo("The entity of type HandEntity with id $zeroId was not found.")
        }

        @Test
        fun `get specific with known id but different owner returns 403`() {
            val round = createPersistedRoundEntity()
            val hand = round.hands.single { it.player.user == testAdmin }

            val response = getResource<ProblemDetailResponse>(
                path = "/v1/hands/${hand.id}",
                expectedStatus = 403
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/hands/${hand.id}")
            assertThat(response.title).isEqualTo("Forbidden action")
            assertThat(response.detail).isEqualTo("You are not allowed to perform the action 'Hand:Show': Only the player holding this hand can show its detailed information.")
        }

        @Test
        fun `get specific with known id and correct owner returns 200 and dto`() {
            val round = createPersistedRoundEntity()
            val player = round.game.players.single { it.user == testUser }
            val hand = round.hands.single { it.player.user == testUser }

            val response = getResource<HandForPlayerDto>(
                path = "/v1/hands/${hand.id}",
                expectedStatus = 200
            )

            assertThat(response.id).isEqualTo(hand.id)
            assertThat(response.playerId).isEqualTo(player.id)
            assertThat(response.roundId).isEqualTo(round.id)
            assertThat(response.cardsRemaining).isEmpty()
            assertThat(response.cardsPlayed).isEmpty()
            assertThat(response.playerTeam).isEqualTo(Team.NA)
        }
    }

    @Nested
    inner class DeclarationsCreate {
        @Test
        fun `declare on unknown uuid returns 404`() {
            val (response, location) = execDeclaration<ProblemDetailResponse>(
                handId = zeroId,
                declarationOption = DeclarationOption.HEALTHY,
                expectedStatus = 404
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/hands/$zeroId/declarations")
            assertThat(response.title).isEqualTo("Entity not found")
            assertThat(response.detail).isEqualTo("The entity of type HandEntity with id 00000000-0000-0000-0000-000000000000 was not found.")

            assertThat(location).isNull()
        }

        @Test
        fun `declare returns 403 when forbidden action exception`() {
            val round = createPersistedRoundEntity()
            val hand = round.hands.single { it.player.user == testAdmin }

            mockkConstructor(HandDeclareModel::class)
            every {
                anyConstructed<HandDeclareModel>().declare(
                    any(),
                    DeclarationOption.HEALTHY
                )
            } throws ForbiddenActionException(
                "Declaration:Create",
                "Mocked Model Exception."
            )

            val (response, location) = execDeclaration<ProblemDetailResponse>(
                handId = hand.id,
                declarationOption = DeclarationOption.HEALTHY,
                expectedStatus = 403
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/hands/${hand.id}/declarations")
            assertThat(response.title).isEqualTo("Forbidden action")
            assertThat(response.detail).isEqualTo("You are not allowed to perform the action 'Declaration:Create': Mocked Model Exception.")

            assertThat(location).isNull()
        }

        @Test
        fun `declare returns 400 when invalid action exception`() {
            val round = createPersistedRoundEntity()
            val hand = round.hands.single { it.player.user == testUser }

            mockkConstructor(HandDeclareModel::class)
            every {
                anyConstructed<HandDeclareModel>().declare(
                    any(),
                    DeclarationOption.HEALTHY
                )
            } throws InvalidActionException(
                "Declaration:Create",
                "Mocked Model Exception."
            )


            val (response, location) = execDeclaration<ProblemDetailResponse>(
                handId = hand.id,
                declarationOption = DeclarationOption.HEALTHY,
                expectedStatus = 400
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/hands/${hand.id}/declarations")
            assertThat(response.title).isEqualTo("Invalid action")
            assertThat(response.detail).isEqualTo("The action 'Declaration:Create' can not be performed: Mocked Model Exception.")

            assertThat(location).isNull()
        }

        @Test
        fun `first declaration on known uuid and correct owner returns 201 and dto of hand`() {
            val round = createPersistedRoundEntity()
            val hand = round.hands.single { it.player.user == testUser }

            mockkConstructor(HandDeclareModel::class)
            every {
                anyConstructed<HandDeclareModel>().declare(
                    any(),
                    DeclarationOption.HEALTHY
                )
            } just Runs
            mockkConstructor(RoundDeclarationEvaluationModel::class)
            every {
                anyConstructed<RoundDeclarationEvaluationModel>().canEvaluateDeclarations()
            } returns Result.failure(mockk())

            val (response, location) = execDeclaration<HandForPlayerDto>(
                handId = hand.id,
                declarationOption = DeclarationOption.HEALTHY,
                expectedStatus = 201
            )

            assertThat(response.id).isEqualTo(hand.id)
            assertThat(response.playerId).isEqualTo(hand.player.id)
            assertThat(response.roundId).isEqualTo(round.id)
            assertThat(response.playerTeam).isEqualTo(Team.NA)
            assertThat(response.cardsPlayed).isEmpty()
            assertThat(response.cardsRemaining).isEmpty()

            assertThat(location).isEqualTo("/v1/hands/${hand.id}")
        }
    }

    @Nested
    inner class BidsCreate {
        @Test
        fun `bid on unknown uuid returns 404`() {
            val (response, location) = execBid<ProblemDetailResponse>(
                handId = zeroId,
                bidOption = BiddingOption.MARRIAGE,
                expectedStatus = 404
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/hands/$zeroId/bids")
            assertThat(response.title).isEqualTo("Entity not found")
            assertThat(response.detail).isEqualTo("The entity of type HandEntity with id 00000000-0000-0000-0000-000000000000 was not found.")

            assertThat(location).isNull()
        }

        @Test
        fun `bid returns 403 when forbidden action exception`() {
            val round = createPersistedRoundEntity()
            val hand = round.hands.single { it.player.user == testUser }

            mockkConstructor(HandBiddingModel::class)
            every {
                anyConstructed<HandBiddingModel>().bid(
                    any(),
                    BiddingOption.MARRIAGE
                )
            } throws ForbiddenActionException(
                "Bid:Create",
                "Mocked Model Exception."
            )

            val (response, location) = execBid<ProblemDetailResponse>(
                handId = hand.id,
                bidOption = BiddingOption.MARRIAGE,
                expectedStatus = 403
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/hands/${hand.id}/bids")
            assertThat(response.title).isEqualTo("Forbidden action")
            assertThat(response.detail).isEqualTo("You are not allowed to perform the action 'Bid:Create': Mocked Model Exception.")

            assertThat(location).isNull()
        }

        @Test
        fun `bid returns 400 when invalid action exception`() {
            val round = createPersistedRoundEntity()
            val hand = round.hands.single { it.player.user == testUser }

            mockkConstructor(HandBiddingModel::class)
            every {
                anyConstructed<HandBiddingModel>().bid(
                    any(),
                    BiddingOption.MARRIAGE
                )
            } throws InvalidActionException(
                "Bid:Create",
                "Mocked Model Exception."
            )


            val (response, location) = execBid<ProblemDetailResponse>(
                handId = hand.id,
                bidOption = BiddingOption.MARRIAGE,
                expectedStatus = 400
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/hands/${hand.id}/bids")
            assertThat(response.title).isEqualTo("Invalid action")
            assertThat(response.detail).isEqualTo("The action 'Bid:Create' can not be performed: Mocked Model Exception.")

            assertThat(location).isNull()
        }

        @Test
        fun `first bid on known uuid and correct owner returns 201 and dto of hand`() {
            val round = createPersistedRoundEntity()
            val hand = round.hands.single { it.player.user == testUser }

            mockkConstructor(HandBiddingModel::class)
            every {
                anyConstructed<HandBiddingModel>().bid(
                    any(),
                    BiddingOption.MARRIAGE
                )
            } just Runs
            mockkConstructor(RoundBidsEvaluationModel::class)
            every {
                anyConstructed<RoundBidsEvaluationModel>().canEvaluateBids()
            } returns Result.failure(mockk())

            val (response, location) = execBid<HandForPlayerDto>(
                handId = hand.id,
                bidOption = BiddingOption.MARRIAGE,
                expectedStatus = 201
            )

            assertThat(response.id).isEqualTo(hand.id)
            assertThat(response.playerId).isEqualTo(hand.player.id)
            assertThat(response.roundId).isEqualTo(round.id)
            assertThat(response.playerTeam).isEqualTo(Team.NA)
            assertThat(response.cardsPlayed).isEmpty()
            assertThat(response.cardsRemaining).isEmpty()

            assertThat(location).isEqualTo("/v1/hands/${hand.id}")
        }
    }

    private fun createPersistedRoundEntity(): RoundEntity {
        val game = GameEntity(
            creator = testAdmin,
            maxNumberOfPlayers = 4
        ).also { game ->
            game.players.add(PlayerEntity(user = testAdmin, game = game, seat = 0))
            game.players.add(PlayerEntity(user = testUser, game = game, seat = 1))

            testPlayers.filter { it != testAdmin && it != testUser }.take(2).mapIndexed { index, userEntity ->
                game.players.add(PlayerEntity(user = userEntity, game = game, seat = index + 2))
            }
            gameRepository.save(game)
            playerRepository.saveAll(game.players)
        }
        val round = RoundEntity(game = game, dealer = game.players.first(), number = 1).apply {
            game.players.forEachIndexed { index, it ->
                hands.add(
                    HandEntity(
                        round = this,
                        player = it,
                        index = index,
                        cardsRemaining = mutableListOf(),
                        hasMarriage = false
                    )
                )
            }
        }.let { roundRepository.save(it) }

        handRepository.saveAll(round.hands)

        return round
    }

    private final inline fun <reified T> execDeclaration(
        handId: UUID,
        declarationOption: DeclarationOption,
        expectedStatus: Int,
    ): Pair<T, String?> {
        return createResource<DeclarationCreateDto, T>(
            path = "/v1/hands/$handId/declarations",
            body = DeclarationCreateDto(
                declaration = declarationOption
            ),
            expectedStatus = expectedStatus
        )
    }

    private final inline fun <reified T> execBid(
        handId: UUID,
        bidOption: BiddingOption,
        expectedStatus: Int
    ): Pair<T, String?> {
        return createResource<BidCreateDto, T>(
            path = "/v1/hands/$handId/bids",
            body = BidCreateDto(
                bid = bidOption
            ),
            expectedStatus = expectedStatus
        )
    }
}