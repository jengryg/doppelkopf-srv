package game.doppelkopf.api.core

import game.doppelkopf.BaseRestAssuredTest
import game.doppelkopf.api.core.dto.turn.CreateTurnDto
import game.doppelkopf.api.core.dto.turn.TurnInfoDto
import game.doppelkopf.core.cards.CardDemand
import game.doppelkopf.core.errors.ForbiddenActionException
import game.doppelkopf.core.errors.GameFailedException
import game.doppelkopf.core.errors.InvalidActionException
import game.doppelkopf.core.model.round.handler.RoundPlayCardModel
import game.doppelkopf.errors.ProblemDetailResponse
import game.doppelkopf.persistence.model.game.GameEntity
import game.doppelkopf.persistence.model.game.GameRepository
import game.doppelkopf.persistence.model.hand.HandEntity
import game.doppelkopf.persistence.model.hand.HandRepository
import game.doppelkopf.persistence.model.player.PlayerEntity
import game.doppelkopf.persistence.model.player.PlayerRepository
import game.doppelkopf.persistence.model.round.RoundEntity
import game.doppelkopf.persistence.model.round.RoundRepository
import game.doppelkopf.persistence.model.trick.TrickEntity
import game.doppelkopf.persistence.model.trick.TrickRepository
import game.doppelkopf.persistence.model.turn.TurnEntity
import game.doppelkopf.persistence.model.turn.TurnRepository
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import kotlin.test.Test

class TurnControllerTest : BaseRestAssuredTest() {
    @Autowired
    private lateinit var trickRepository: TrickRepository

    @Autowired
    private lateinit var gameRepository: GameRepository

    @Autowired
    private lateinit var playerRepository: PlayerRepository

    @Autowired
    private lateinit var roundRepository: RoundRepository

    @Autowired
    private lateinit var handRepository: HandRepository

    @Autowired
    private lateinit var turnRepository: TurnRepository


    @BeforeEach
    @AfterEach
    fun `unmock all`() {
        clearAllMocks()
        unmockkAll()
    }

    @Nested
    inner class GetAndList {
        @Test
        fun `get turns of round that has no turns returns empty list`() {
            val round = createPersistedRoundEntity()

            val response = getResourceList<TurnInfoDto>(
                path = "/v1/rounds/${round.id}/turns",
                expectedStatus = 200
            )

            assertThat(response).isEmpty()
        }

        @Test
        fun `get turns of round with unknown uuid returns 404`() {
            val response = getResource<ProblemDetailResponse>(
                path = "/v1/rounds/$zeroId/turns",
                expectedStatus = 404
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/rounds/$zeroId/turns")
            assertThat(response.title).isEqualTo("Entity not found")
            assertThat(response.detail).isEqualTo("The entity of type RoundEntity with id $zeroId was not found.")
        }

        @Test
        fun `get turns of round that has some turns returns 200 with list of dto`() {
            val round = createPersistedRoundEntity().apply {
                hands.mapIndexed { i, h ->
                    tricks.mapIndexed { j, t ->
                        turns.add(
                            TurnEntity(
                                round = this,
                                hand = h,
                                trick = t,
                                number = i * tricks.size + j,
                                card = "QC0"
                            )
                        )
                    }
                }
            }

            turnRepository.saveAll(round.turns)

            val response = getResourceList<TurnInfoDto>(
                path = "/v1/rounds/${round.id}/turns",
                expectedStatus = 200
            )

            assertThat(response).hasSize(12)
            assertThat(response.map { it.id }).containsExactlyInAnyOrderElementsOf(round.turns.map { it.id })

            response.forEach {
                assertThat(it.roundId).isEqualTo(round.id)
                assertThat(it.card).isEqualTo("QC0")
                assertThat(it.handId).isIn(round.hands.map { h -> h.id })
                assertThat(it.trickId).isIn(round.tricks.map { t -> t.id })
            }
        }

        @Test
        fun `get specific turn with unknown uuid returns 404`() {
            val response = getResource<ProblemDetailResponse>(
                path = "/v1/turns/$zeroId",
                expectedStatus = 404
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/turns/$zeroId")
            assertThat(response.title).isEqualTo("Entity not found")
            assertThat(response.detail).isEqualTo("The entity of type TurnEntity with id $zeroId was not found.")
        }

        @Test
        fun `get specific turn with known uuid returns 200 and dto`() {
            val round = createPersistedRoundEntity()
            val hand = round.hands.first()
            val trick = round.tricks.first()

            val turn = TurnEntity(round = round, hand = hand, trick = trick, number = 1, card = "QC0").also {
                round.turns.add(it)
            }.let { turnRepository.save(it) }

            val response = getResource<TurnInfoDto>(
                path = "/v1/turns/${turn.id}",
                expectedStatus = 200
            )

            assertThat(response.id).isEqualTo(turn.id)
            assertThat(response.roundId).isEqualTo(round.id)
            assertThat(response.handId).isEqualTo(hand.id)
            assertThat(response.trickId).isEqualTo(trick.id)
            assertThat(response.number).isEqualTo(1)
            assertThat(response.card).isEqualTo("QC0")
        }
    }

    @Nested
    inner class Create {
        @Test
        fun `turn creation in valid case returns 201 and dto`() {
            val round = createPersistedRoundEntity()
            val trick = round.tricks.first()
            val hand = round.hands.first()
            val turn = TurnEntity(
                round = round,
                hand = hand,
                trick = trick,
                number = 17,
                card = "TH0"
            )
            round.turns.add(turn)

            mockkConstructor(RoundPlayCardModel::class)
            every { anyConstructed<RoundPlayCardModel>().playCard(any(), any()) } returns Pair(
                first = mockk { every { entity } returns trick },
                second = mockk { every { entity } returns turn }
            )

            val (response, location) = execPlayCard<TurnInfoDto>(round.id, 201)

            response.also {
                assertThat(it.roundId).isEqualTo(round.id)
                assertThat(it.handId).isEqualTo(hand.id)
                assertThat(it.trickId).isEqualTo(trick.id)
                assertThat(it.number).isEqualTo(17)
                assertThat(it.card).isEqualTo("TH0")
            }

            assertThat(location).isEqualTo("/v1/turns/${turn.id}")
        }

        @Test
        fun `create returns 400 when invalid action exception`() {
            val round = createPersistedRoundEntity()

            mockkConstructor(RoundPlayCardModel::class)
            every { anyConstructed<RoundPlayCardModel>().playCard(any(), any()) } throws InvalidActionException(
                "Play:Card",
                "Mocked Model Exception"
            )

            val (response, location) = execPlayCard<ProblemDetailResponse>(round.id, 400)

            assertThat(response.instance.toString()).isEqualTo("/v1/rounds/${round.id}/turns")
            assertThat(response.title).isEqualTo("Invalid action")
            assertThat(response.detail).isEqualTo("The action 'Play:Card' can not be performed: Mocked Model Exception")

            assertThat(location).isNull()
        }

        @Test
        fun `create returns 403 when forbidden action exception`() {
            val round = createPersistedRoundEntity()

            mockkConstructor(RoundPlayCardModel::class)
            every { anyConstructed<RoundPlayCardModel>().playCard(any(), any()) } throws ForbiddenActionException(
                "Play:Card",
                "Mocked Model Exception"
            )

            val (response, location) = execPlayCard<ProblemDetailResponse>(round.id, 403)

            assertThat(response.instance.toString()).isEqualTo("/v1/rounds/${round.id}/turns")
            assertThat(response.title).isEqualTo("Forbidden action")
            assertThat(response.detail).isEqualTo("You are not allowed to perform the action 'Play:Card': Mocked Model Exception")

            assertThat(location).isNull()
        }

        @Test
        fun `create returns 500 when game failed exception`() {
            val round = createPersistedRoundEntity()

            mockkConstructor(RoundPlayCardModel::class)
            every { anyConstructed<RoundPlayCardModel>().playCard(any(), any()) } throws GameFailedException(
                "Mocked Model Exception",
                round.id
            )

            val (response, location) = execPlayCard<ProblemDetailResponse>(round.id, 500)

            assertThat(response.instance.toString()).isEqualTo("/v1/rounds/${round.id}/turns")
            assertThat(response.title).isEqualTo("Game Failed")
            assertThat(response.detail).isEqualTo("An error occurred during game processing: Mocked Model Exception")

            assertThat(location).isNull()
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
            repeat(3) {
                tricks.add(
                    TrickEntity(
                        round = this,
                        number = it,
                        openIndex = it,
                        demand = CardDemand.CLUBS
                    )
                )
            }
        }.let { roundRepository.save(it) }

        handRepository.saveAll(round.hands)
        trickRepository.saveAll(round.tricks)

        return round
    }

    private final inline fun <reified T> execPlayCard(
        roundId: UUID,
        expectedStatus: Int
    ): Pair<T, String?> {
        return createResource<CreateTurnDto, T>(
            path = "/v1/rounds/$roundId/turns",
            body = CreateTurnDto(card = "TH0"),
            expectedStatus = expectedStatus
        )
    }
}