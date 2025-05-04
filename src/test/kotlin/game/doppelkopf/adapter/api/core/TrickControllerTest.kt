package game.doppelkopf.adapter.api.core

import game.doppelkopf.BaseRestAssuredTest
import game.doppelkopf.adapter.api.core.trick.dto.TrickInfoDto
import game.doppelkopf.adapter.api.core.trick.dto.TrickOperationDto
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.game.GameRepository
import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.adapter.persistence.model.hand.HandRepository
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.adapter.persistence.model.player.PlayerRepository
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.round.RoundRepository
import game.doppelkopf.adapter.persistence.model.trick.TrickEntity
import game.doppelkopf.adapter.persistence.model.trick.TrickRepository
import game.doppelkopf.domain.deck.enums.CardDemand
import game.doppelkopf.domain.trick.enums.TrickOperation
import game.doppelkopf.common.errors.InvalidActionException
import game.doppelkopf.domain.trick.service.TrickEvaluationModel
import game.doppelkopf.errors.ProblemDetailResponse
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class TrickControllerTest : BaseRestAssuredTest() {
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

    @BeforeEach
    @AfterEach
    fun `unmock all`() {
        clearAllMocks()
        unmockkAll()
    }

    @Nested
    inner class GetAndList {
        @Test
        fun `get tricks of round that has no tricks returns empty list`() {
            val round = createPersistedRoundEntity()

            val response = getResourceList<TrickInfoDto>(
                path = "/v1/rounds/${round.id}/tricks",
                expectedStatus = 200
            )

            assertThat(response).isEmpty()
        }

        @Test
        fun `get tricks of round with unknown uuid returns 404`() {
            val response = getResource<ProblemDetailResponse>(
                path = "/v1/rounds/$zeroId/tricks",
                expectedStatus = 404
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/rounds/$zeroId/tricks")
            assertThat(response.title).isEqualTo("Entity not found")
            assertThat(response.detail).isEqualTo("The entity of type RoundEntity with id $zeroId was not found.")
        }

        @Test
        fun `get tricks of round that has some tricks returns 200 with list of dto`() {
            val round = createPersistedRoundEntity().apply {
                tricks.add(
                    TrickEntity(
                        round = this,
                        number = 1,
                        openIndex = 1,
                        demand = CardDemand.CLUBS
                    ).also {
                        it.winner == hands.first()
                        it.cards.addAll(listOf("QC0", "QC1", "AS0", "AS1"))
                    }
                )
                tricks.add(
                    TrickEntity(
                        round = this,
                        number = 2,
                        openIndex = 1,
                        demand = CardDemand.HEARTS
                    ).also {
                        it.winner = hands.last()
                        it.cards.addAll(listOf("KH0", "QH1", "TD0", "TH1"))
                    }
                )
            }

            trickRepository.saveAll(round.tricks)

            val response = getResourceList<TrickInfoDto>(
                path = "/v1/rounds/${round.id}/tricks",
                expectedStatus = 200
            )

            assertThat(response).hasSize(2)
            assertThat(response.map { it.id }).containsExactlyInAnyOrderElementsOf(round.tricks.map { it.id })

            response.forEach {
                assertThat(it.roundId).isEqualTo(round.id)
                assertThat(it.openIndex).isEqualTo(1)
                assertThat(it.cards).hasSize(4)
            }
        }

        @Test
        fun `get specific trick with unknown uuid returns 404`() {
            val response = getResource<ProblemDetailResponse>(
                path = "/v1/tricks/$zeroId",
                expectedStatus = 404
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/tricks/$zeroId")
            assertThat(response.title).isEqualTo("Entity not found")
            assertThat(response.detail).isEqualTo("The entity of type TrickEntity with id $zeroId was not found.")
        }

        @Test
        fun `get specific trick with known uuid returns 200 and dto`() {
            val round = createPersistedRoundEntity().apply {
                tricks.add(
                    TrickEntity(
                        round = this,
                        number = 1,
                        openIndex = 1,
                        demand = CardDemand.CLUBS
                    ).also {
                        it.cards.addAll(listOf("QC0", "QC1", "AS0"))
                    }
                )
            }

            val trick = trickRepository.save(round.tricks.first())

            val response = getResource<TrickInfoDto>(
                path = "/v1/tricks/${trick.id}",
                expectedStatus = 200
            )

            assertThat(response.id).isEqualTo(trick.id)
            assertThat(response.roundId).isEqualTo(round.id)
            assertThat(response.number).isEqualTo(trick.number)
            assertThat(response.openIndex).isEqualTo(trick.openIndex)
            assertThat(response.demand).isEqualTo(trick.demand)
            assertThat(response.cards).containsExactlyElementsOf(trick.cards)
            assertThat(response.leadingCardIndex).isEqualTo(trick.leadingCardIndex)
            assertThat(response.winner).isEqualTo(null)
        }
    }

    @Nested
    inner class EvalTrick {
        @Test
        fun `eval trick returns 200 when processor successful`() {
            val round = createPersistedRoundEntity()

            val trick = TrickEntity(
                round = round,
                number = 1,
                openIndex = 1,
                demand = CardDemand.CLUBS
            ).let { trickRepository.save(it) }.also {
                round.tricks.add(it)
            }

            mockkConstructor(TrickEvaluationModel::class)
            every { anyConstructed<TrickEvaluationModel>().evaluateTrick() } just Runs

            val response = execPatchTrick<TrickInfoDto>(trick.id, TrickOperation.TRICK_EVALUATION, 200)

            assertThat(response.id).isEqualTo(trick.id)
        }

        @Test
        fun `eval trick returns 400 when invalid action exception`() {
            val round = createPersistedRoundEntity()

            val trick = TrickEntity(
                round = round,
                number = 1,
                openIndex = 1,
                demand = CardDemand.CLUBS
            ).let { trickRepository.save(it) }.also {
                round.tricks.add(it)
            }

            mockkConstructor(TrickEvaluationModel::class)
            every { anyConstructed<TrickEvaluationModel>().evaluateTrick() } throws InvalidActionException(
                "Trick:Evaluate",
                "Mocked Model Exception."
            )

            val response = execPatchTrick<ProblemDetailResponse>(trick.id, TrickOperation.TRICK_EVALUATION, 400)

            response.also {
                assertThat(response.instance.toString()).isEqualTo("/v1/tricks/${trick.id}")
                assertThat(response.title).isEqualTo("Invalid action")
                assertThat(response.detail).isEqualTo("The action 'Trick:Evaluate' can not be performed: Mocked Model Exception.")
            }
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
        trickRepository.saveAll(round.tricks)

        return round
    }

    private final inline fun <reified T> execPatchTrick(
        trickId: UUID,
        operation: TrickOperation,
        expectedStatus: Int
    ): T {
        return patchResource<TrickOperationDto, T>(
            path = "/v1/tricks/$trickId",
            body = TrickOperationDto(
                op = operation,
            ),
            expectedStatus = expectedStatus
        )
    }
}