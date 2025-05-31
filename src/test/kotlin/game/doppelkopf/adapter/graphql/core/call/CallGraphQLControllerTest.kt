package game.doppelkopf.adapter.graphql.core.call

import game.doppelkopf.BaseGraphQLTest
import game.doppelkopf.adapter.graphql.core.call.dto.CallResponse
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
import game.doppelkopf.domain.hand.enums.Team
import game.doppelkopf.domain.hand.service.HandCallModel
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

class CallGraphQLControllerTest : BaseGraphQLTest() {
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
    inner class Queries {
        @Test
        fun `get specific with unknown uuid returns error`() {
            getCall(zeroId)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Entity not found")
                }.verify()
        }

        @Test
        fun `get specified with known uuid returns data`() {
            val hand = createPersistedHandEntity()[testUser.id]!!
            val entity = CallEntity(
                hand = hand,
                callType = CallType.UNDER_120,
                cardsPlayedBefore = 0
            ).also {
                hand.calls.add(it)
            }.let { callRepository.save(it) }

            val response = getCall(entity.id)
                .execute()
                .toSingleEntity<CallResponse>()

            assertThat(response.id).isEqualTo(entity.id)
            assertThat(response.cardsPlayedBefore).isEqualTo(entity.cardsPlayedBefore)
            assertThat(response.callType).isEqualTo(entity.callType)
            assertThat(response.cu.created).isEqualTo(entity.created)
            assertThat(response.cu.updated).isEqualTo(entity.updated)
        }
    }

    @Nested
    inner class Mutations {
        @Test
        fun `make call on hand with unknown uuid returns error`() {
            makeCall(zeroId, CallType.UNDER_120)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Entity not found")
                }.verify()

        }

        @Test
        fun `make call returns error when forbidden action exception`() {
            val hand = createPersistedHandEntity()[testUser.id]!!

            mockkConstructor(HandCallModel::class)
            every {
                anyConstructed<HandCallModel>().makeCall(
                    any(),
                    CallType.UNDER_120,
                )
            } throws ForbiddenActionException(
                "Mocked Model Exception."
            )

            makeCall(hand.id, CallType.UNDER_120)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Forbidden action")
                }.verify()
        }

        @Test
        fun `make call returns error when invalid action exception`() {
            val hand = createPersistedHandEntity()[testUser.id]!!

            mockkConstructor(HandCallModel::class)
            every {
                anyConstructed<HandCallModel>().makeCall(
                    any(),
                    CallType.UNDER_120,
                )
            } throws InvalidActionException(
                "Mocked Model Exception."
            )

            makeCall(hand.id, CallType.UNDER_120)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Invalid action")
                }.verify()
        }

        @Test
        fun `make call creates and returns call for valid data`() {
            val hand = createPersistedHandEntity()[testUser.id]!!

            val response = makeCall(hand.id, CallType.UNDER_120)
                .execute()
                .toSingleEntity<CallResponse>()

            assertThat(response.callType).isEqualTo(CallType.UNDER_120)
            assertThat(response.cardsPlayedBefore).isEqualTo(hand.cardsPlayed.size)
            assertThat(response.description).isNotBlank
        }
    }

    private fun getCall(callId: UUID): GraphQlTester.Request<*> {
        return gqlUserTester
            .documentName("getCall")
            .fragName("callProperties")
            .fragName("cu")
            .variable("id", callId)
    }

    private fun makeCall(handId: UUID, callType: CallType): GraphQlTester.Request<*> {
        return gqlUserTester
            .documentName("makeCall")
            .fragName("callProperties")
            .fragName("cu")
            .variable("handId", handId)
            .variable("callType", callType)
    }

    private fun createPersistedHandEntity(): Map<UUID, HandEntity> {
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

        val round = RoundEntity(
            game = game,
            dealer = game.players.first(),
            number = 1,
            seed = ByteArray(256)
        ).apply {
            state = RoundState.PLAYING_TRICKS
        }.let { roundRepository.save(it) }

        val hands = game.players.mapIndexed { index, it ->
            it.user.id to HandEntity(
                round = round,
                player = it,
                index = index,
                cardsRemaining = mutableListOf(),
                hasMarriage = false
            ).also {
                it.internalTeam = Team.RE
                round.hands.add(it)
            }.let {
                handRepository.save(it)
            }
        }.toMap()

        return hands
    }
}