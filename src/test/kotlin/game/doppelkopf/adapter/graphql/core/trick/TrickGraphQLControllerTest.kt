package game.doppelkopf.adapter.graphql.core.trick

import game.doppelkopf.BaseGraphQLTest
import game.doppelkopf.adapter.graphql.core.trick.dto.TrickResponse
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
import game.doppelkopf.fragName
import game.doppelkopf.toSingleEntity
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.test.tester.GraphQlTester
import java.util.*

class TrickGraphQLControllerTest : BaseGraphQLTest() {
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
    inner class Queries {
        @Test
        fun `get specific with unknown uuid returns error`() {
            getTrick(zeroId)
                .execute()
                .errors()
                .expect { error ->
                    error.message!!.contains("Entity not found")
                }.verify()
        }

        @Test
        fun `get specific with known uuid returns data`() {
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

            val response = getTrick(trick.id)
                .execute()
                .toSingleEntity<TrickResponse>()

            // TODO
            assertThat(response.id).isEqualTo(trick.id)
            assertThat(response.openIndex).isEqualTo(trick.openIndex)
            assertThat(response.demand).isEqualTo(trick.demand)
            assertThat(response.number).isEqualTo(trick.number)
        }
    }

    private fun getTrick(trickId: UUID): GraphQlTester.Request<*> {
        return gqlUserTester
            .documentName("getTrick")
            .fragName("trickProperties")
            .fragName("cu")
            .fragName("se")
            .variable("id", trickId)
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
        }.let { roundRepository.save(it) }

        handRepository.saveAll(round.hands)
        trickRepository.saveAll(round.tricks)

        return round
    }
}