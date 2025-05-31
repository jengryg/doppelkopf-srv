package game.doppelkopf.adapter.graphql.core.hand

import game.doppelkopf.BaseGraphQLTest
import game.doppelkopf.adapter.graphql.core.hand.dto.PrivateHandResponse
import game.doppelkopf.adapter.graphql.core.hand.dto.PublicHandResponse
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.game.GameRepository
import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.adapter.persistence.model.hand.HandRepository
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.adapter.persistence.model.player.PlayerRepository
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.round.RoundRepository
import game.doppelkopf.common.errors.ForbiddenActionException
import game.doppelkopf.common.errors.InvalidActionException
import game.doppelkopf.domain.hand.enums.*
import game.doppelkopf.domain.hand.service.HandBiddingModel
import game.doppelkopf.domain.hand.service.HandDeclareModel
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

class HandGraphQLControllerTest : BaseGraphQLTest() {
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
    inner class Queries {
        @Test
        fun `get specific with unknown uuid returns error for public and private`() {
            getPublicHand(zeroId).execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Entity not found")
                }.verify()

            getPrivateHand(zeroId).execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Entity not found")
                }.verify()
        }

        @Test
        fun `get specific with known uuid returns public hand`() {
            val round = createPersistedRoundEntity()
            val hand = round.hands.single { it.player.user == testAdmin }

            val response = getPublicHand(hand.id)
                .execute()
                .toSingleEntity<PublicHandResponse>()

            assertThat(response.id).isEqualTo(hand.id)
            // TODO
        }

        @Test
        fun `get specific with known uuid returns forbidden error when user is not the owner`() {
            val round = createPersistedRoundEntity()
            val hand = round.hands.single { it.player.user == testAdmin }

            getPrivateHand(hand.id)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Forbidden action")
                }.verify()
        }

        @Test
        fun `get specific with known uuid return private hand if user is the owner`() {
            val round = createPersistedRoundEntity()
            val hand = round.hands.single { it.player.user == testUser }

            val response = getPrivateHand(hand.id)
                .execute()
                .toSingleEntity<PrivateHandResponse>()

            assertThat(response.id).isEqualTo(hand.id)
            // TODO
        }
    }

    @Nested
    inner class Mutations {
        @Test
        fun `declare on unknown uuid returns error`() {
            declareHand(zeroId, DeclarationOption.HEALTHY)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Entity not found")
                }.verify()
        }

        @Test
        fun `declare modifies hand and returns private hand when valid`() {
            val round = createPersistedRoundEntity()
            val hand = round.hands.single { it.player.user == testUser }

            val response = declareHand(hand.id, DeclarationOption.HEALTHY)
                .execute()
                .toSingleEntity<PrivateHandResponse>()

            // TODO
            assertThat(response.id).isEqualTo(hand.id)
            assertThat(response.public.declared).isEqualTo(DeclarationPublic.HEALTHY)
        }

        @Test
        fun `declare hand returns error when forbidden action exception`() {
            val round = createPersistedRoundEntity()
            val hand = round.hands.single { it.player.user == testAdmin }

            mockkConstructor(HandDeclareModel::class)
            every {
                anyConstructed<HandDeclareModel>().declare(
                    any(),
                    DeclarationOption.HEALTHY
                )
            } throws ForbiddenActionException(
                "Mocked Model Exception."
            )

            declareHand(hand.id, DeclarationOption.HEALTHY)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Forbidden action")
                }.verify()
        }

        @Test
        fun `declare hand returns error when invalid action exception`() {
            val round = createPersistedRoundEntity()
            val hand = round.hands.single { it.player.user == testUser }

            mockkConstructor(HandDeclareModel::class)
            every {
                anyConstructed<HandDeclareModel>().declare(
                    any(),
                    DeclarationOption.HEALTHY
                )
            } throws InvalidActionException(
                "Mocked Model Exception."
            )

            declareHand(hand.id, DeclarationOption.HEALTHY)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Invalid action")
                }.verify()
        }

        @Test
        fun `bid on unknown uuid returns error`() {
            bidHand(zeroId, BiddingOption.MARRIAGE)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Entity not found")
                }.verify()
        }

        @Test
        fun `bid modifies hand and returns private hand when valid`() {
            val round = createPersistedRoundEntity()
            val hand =
                round.hands.single { it.player.user == testUser }.apply { declared = Declaration.RESERVATION }.let {
                    handRepository.save(it)
                }

            val response = bidHand(hand.id, BiddingOption.SOLO_DIAMONDS)
                .execute()
                .toSingleEntity<PrivateHandResponse>()

            // TODO
            assertThat(response.id).isEqualTo(hand.id)
            assertThat(response.public.declared).isEqualTo(DeclarationPublic.RESERVATION)
            assertThat(response.public.bid).isEqualTo(BiddingPublic.SOLO_DIAMONDS)
        }

        @Test
        fun `bid hand returns error when forbidden action exception`() {
            val round = createPersistedRoundEntity()
            val hand = round.hands.single { it.player.user == testUser }

            mockkConstructor(HandBiddingModel::class)
            every {
                anyConstructed<HandBiddingModel>().bid(
                    any(),
                    BiddingOption.MARRIAGE
                )
            } throws ForbiddenActionException(
                "Mocked Model Exception."
            )

            bidHand(hand.id, BiddingOption.MARRIAGE)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Forbidden action")
                }.verify()
        }

        @Test
        fun `bid hand returns error when invalid action exception`() {
            val round = createPersistedRoundEntity()
            val hand = round.hands.single { it.player.user == testUser }

            mockkConstructor(HandBiddingModel::class)
            every {
                anyConstructed<HandBiddingModel>().bid(
                    any(),
                    BiddingOption.MARRIAGE
                )
            } throws InvalidActionException(
                "Mocked Model Exception."
            )

            bidHand(hand.id, BiddingOption.MARRIAGE)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Invalid action")
                }.verify()
        }
    }

    private fun getPublicHand(handId: UUID): GraphQlTester.Request<*> {
        return gqlUserTester.documentName("getPublicHand")
            .fragName("publicHandProperties")
            .fragName("cu")
            .variable("id", handId)

    }

    private fun getPrivateHand(handId: UUID): GraphQlTester.Request<*> {
        return gqlUserTester.documentName("getPrivateHand")
            .fragName("privateHandProperties")
            .fragName("publicHandProperties")
            .fragName("cu")
            .variable("id", handId)
    }

    private fun declareHand(handId: UUID, declarationOption: DeclarationOption): GraphQlTester.Request<*> {
        return gqlUserTester.documentName("declareHand")
            .fragName("privateHandProperties")
            .fragName("publicHandProperties")
            .fragName("cu")
            .variable("handId", handId)
            .variable("declaration", declarationOption)
    }

    private fun bidHand(handId: UUID, biddingOption: BiddingOption): GraphQlTester.Request<*> {
        return gqlUserTester.documentName("bidHand")
            .fragName("privateHandProperties")
            .fragName("publicHandProperties")
            .fragName("cu")
            .variable("handId", handId)
            .variable("bidding", biddingOption)
    }

    private fun createPersistedRoundEntity(): RoundEntity {
        val game = GameEntity(
            creator = testAdmin,
            maxNumberOfPlayers = 4,
            seed = ByteArray(256),
        ).also { game ->
            game.players.add(PlayerEntity(user = testAdmin, game = game, seat = 0))
            game.players.add(PlayerEntity(user = testUser, game = game, seat = 1))

            testPlayers.filter { it != testAdmin && it != testUser }.take(2).mapIndexed { index, userEntity ->
                game.players.add(PlayerEntity(user = userEntity, game = game, seat = index + 2))
            }
            gameRepository.save(game)
            playerRepository.saveAll(game.players)
        }
        val round = RoundEntity(game = game, dealer = game.players.first(), number = 1, seed = ByteArray(256)).apply {
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
}