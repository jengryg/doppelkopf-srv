package game.doppelkopf.adapter.api.core

import game.doppelkopf.BaseRestAssuredTest
import game.doppelkopf.adapter.api.core.call.dto.CallCreateRequest
import game.doppelkopf.adapter.api.core.call.dto.CallInfoResponse
import game.doppelkopf.adapter.persistence.model.call.CallEntity
import game.doppelkopf.adapter.persistence.model.call.CallRepository
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
import game.doppelkopf.domain.call.enums.CallType
import game.doppelkopf.domain.hand.service.HandCallModel
import game.doppelkopf.errors.ProblemDetailResponse
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
import java.util.*

class CallControllerTest : BaseRestAssuredTest() {
    @Autowired
    private lateinit var callRepository: CallRepository

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
        fun `get calls of hand that has no calls returns empty list`() {
            val hand = createPersistedHandEntity()

            val response = getResourceList<CallInfoResponse>(
                path = "/v1/hands/${hand.id}/calls",
                expectedStatus = 200
            )

            assertThat(response).isEmpty()
        }

        @Test
        fun `get calls of hand with unknown id returns 404`() {
            val response = getResource<ProblemDetailResponse>(
                path = "/v1/hands/$zeroId/calls",
                expectedStatus = 404
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/hands/$zeroId/calls")
            assertThat(response.title).isEqualTo("Entity not found")
            assertThat(response.detail).isEqualTo("The entity of type HandEntity with id $zeroId was not found.")
        }

        @Test
        fun `get calls of round that has some calls returns 200 with list of dto`() {
            val hand = createPersistedHandEntity().apply {
                calls.add(
                    CallEntity(
                        hand = this,
                        callType = CallType.UNDER_120,
                        cardsPlayedBefore = 1
                    )
                )
                calls.add(
                    CallEntity(
                        hand = this,
                        callType = CallType.UNDER_90,
                        cardsPlayedBefore = 2
                    )
                )
            }

            callRepository.saveAll(hand.calls)

            val response = getResourceList<CallInfoResponse>(
                path = "/v1/hands/${hand.id}/calls",
                expectedStatus = 200
            )

            assertThat(response).hasSize(2)
            assertThat(response.map { it.id }).containsExactlyInAnyOrderElementsOf(hand.calls.map { it.id })

            response.forEach {
                assertThat(it.handId).isEqualTo(hand.id)
                assertThat(it.callType).isIn(CallType.UNDER_120, CallType.UNDER_90)
            }
        }

        @Test
        fun `get specific call with unknown uuid returns 404`() {
            val response = getResource<ProblemDetailResponse>(
                path = "/v1/calls/$zeroId",
                expectedStatus = 404
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/calls/$zeroId")
            assertThat(response.title).isEqualTo("Entity not found")
            assertThat(response.detail).isEqualTo("The entity of type CallEntity with id $zeroId was not found.")
        }

        @Test
        fun `get specific call with known uuid returns 200 and dto`() {
            val hand = createPersistedHandEntity()
            val call = CallEntity(
                hand = hand,
                callType = CallType.UNDER_120,
                cardsPlayedBefore = 0
            ).also {
                hand.calls.add(it)
            }.let { callRepository.save(it) }


            val response = getResource<CallInfoResponse>(
                path = "/v1/calls/${call.id}",
                expectedStatus = 200
            )

            assertThat(response.id).isEqualTo(call.id)
            assertThat(response.callType).isEqualTo(CallType.UNDER_120)
            assertThat(response.cardsPlayedBefore).isEqualTo(call.cardsPlayedBefore)
        }
    }

    @Nested
    inner class CallCreate {
        @Test
        fun `call on unknown uuid returns 404`() {
            val (response, location) = execCall<ProblemDetailResponse>(
                handId = zeroId,
                callType = CallType.UNDER_120,
                expectedStatus = 404
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/hands/$zeroId/calls")
            assertThat(response.title).isEqualTo("Entity not found")
            assertThat(response.detail).isEqualTo("The entity of type HandEntity with id 00000000-0000-0000-0000-000000000000 was not found.")

            assertThat(location).isNull()
        }

        @Test
        fun `call returns 403 when forbidden action exception`() {
            val hand = createPersistedHandEntity()

            mockkConstructor(HandCallModel::class)
            every {
                anyConstructed<HandCallModel>().makeCall(
                    any(),
                    CallType.UNDER_120,
                )
            } throws ForbiddenActionException(
                "Mocked Model Exception."
            )

            val (response, location) = execCall<ProblemDetailResponse>(
                handId = hand.id,
                callType = CallType.UNDER_120,
                expectedStatus = 403
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/hands/${hand.id}/calls")
            assertThat(response.title).isEqualTo("Forbidden action")
            assertThat(response.detail).isEqualTo("You are not allowed to perform this action: Mocked Model Exception.")

            assertThat(location).isNull()
        }

        @Test
        fun `call returns 400 when invalid action exception`() {
            val hand = createPersistedHandEntity()

            mockkConstructor(HandCallModel::class)
            every {
                anyConstructed<HandCallModel>().makeCall(
                    any(),
                    CallType.UNDER_120,
                )
            } throws InvalidActionException(
                "Mocked Model Exception."
            )

            val (response, location) = execCall<ProblemDetailResponse>(
                handId = hand.id,
                callType = CallType.UNDER_120,
                expectedStatus = 400
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/hands/${hand.id}/calls")
            assertThat(response.title).isEqualTo("Invalid action")
            assertThat(response.detail).isEqualTo("This action can not be performed: Mocked Model Exception.")

            assertThat(location).isNull()
        }
    }

    private fun createPersistedHandEntity(): HandEntity {
        val game = GameEntity(
            creator = testAdmin,
            maxNumberOfPlayers = 4,
            seed = ByteArray(256)
        ).also { game ->
            game.players.add(PlayerEntity(user = testAdmin, game = game, seat = 0))
            game.players.add(PlayerEntity(user = testUser, game = game, seat = 1))

            testPlayers.filter { it != testAdmin && it != testUser }.take(2).mapIndexed { index, userEntity ->
                game.players.add(PlayerEntity(user = userEntity, game = game, seat = index + 2))
            }
        }

        gameRepository.save(game)
        playerRepository.saveAll(game.players)

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

        return handRepository.saveAll(round.hands).first()
    }

    private final inline fun <reified T> execCall(
        handId: UUID,
        callType: CallType,
        expectedStatus: Int
    ): Pair<T, String?> {
        return createResource<CallCreateRequest, T>(
            path = "/v1/hands/$handId/calls",
            body = CallCreateRequest(
                callType = callType
            ),
            expectedStatus = expectedStatus
        )
    }
}