package game.doppelkopf.adapter.graphql.core.turn

import game.doppelkopf.BaseGraphQLTest
import game.doppelkopf.adapter.graphql.core.turn.dto.TurnResponse
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
import game.doppelkopf.adapter.persistence.model.turn.TurnEntity
import game.doppelkopf.adapter.persistence.model.turn.TurnRepository
import game.doppelkopf.common.errors.ForbiddenActionException
import game.doppelkopf.common.errors.InvalidActionException
import game.doppelkopf.domain.deck.enums.CardDemand
import game.doppelkopf.domain.round.service.RoundPlayCardModel
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

class TurnGraphQLControllerTest : BaseGraphQLTest() {
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
    inner class Queries {
        @Test
        fun `get specific with unknown uuid returns error`() {
            getTurn(zeroId)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Entity not found")
                }.verify()
        }

        @Test
        fun `get specific with known uuid returns data`() {
            val round = createPersistedRoundEntity()
            val hand = round.hands.first()
            val trick = round.tricks.first()

            val turn = TurnEntity(round = round, hand = hand, trick = trick, number = 1, card = "QC0").also {
                round.turns.add(it)
            }.let { turnRepository.save(it) }

            val response = getTurn(turn.id)
                .execute()
                .toSingleEntity<TurnResponse>()

            assertThat(response.id).isEqualTo(turn.id)
            assertThat(response.number).isEqualTo(1)
            assertThat(response.card).isEqualTo("QC0")
        }
    }

    @Nested
    inner class Mutations {
        @Test
        fun `play card creates turn and returns data`() {
            // TODO work with seeded random to create a working test case here
        }

        @Test
        fun `play card returns error when forbidden action exception`() {
            val round = createPersistedRoundEntity()

            mockkConstructor(RoundPlayCardModel::class)
            every { anyConstructed<RoundPlayCardModel>().playCard(any(), any()) } throws ForbiddenActionException(
                "Mocked Model Exception."
            )

            val response = playCard(round.id, "QC0")
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Forbidden action")
                }.verify()
        }

        @Test
        fun `play card returns error when invalid action exception`() {
            val round = createPersistedRoundEntity()

            mockkConstructor(RoundPlayCardModel::class)
            every { anyConstructed<RoundPlayCardModel>().playCard(any(), any()) } throws InvalidActionException(
                "Mocked Model Exception."
            )

            val response = playCard(round.id, "QC0")
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Invalid action")
                }.verify()
        }
    }

    private fun getTurn(turnId: UUID): GraphQlTester.Request<*> {
        return gqlUserTester
            .documentName("getTurn")
            .fragName("turnProperties")
            .fragName("cu")
            .variable("id", turnId)
    }

    private fun playCard(roundId: UUID, card: String): GraphQlTester.Request<*> {
        return gqlUserTester
            .documentName("playCard")
            .fragName("turnProperties")
            .fragName("cu")
            .variable("roundId", roundId)
            .variable("card", card)
    }

    private fun createPersistedRoundEntity(): RoundEntity {
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
}