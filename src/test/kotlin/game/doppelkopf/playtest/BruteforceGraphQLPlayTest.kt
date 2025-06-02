package game.doppelkopf.playtest

import com.fasterxml.jackson.databind.ObjectMapper
import game.doppelkopf.BaseGraphQLTest
import game.doppelkopf.adapter.graphql.core.game.dto.GameResponse
import game.doppelkopf.adapter.graphql.core.round.dto.RoundResponse
import game.doppelkopf.domain.deck.enums.DeckMode
import game.doppelkopf.domain.deck.model.Deck
import game.doppelkopf.domain.game.enums.GameState
import game.doppelkopf.domain.hand.enums.BiddingOption
import game.doppelkopf.domain.hand.enums.DeclarationOption
import game.doppelkopf.domain.round.enums.RoundState
import game.doppelkopf.fragName
import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import game.doppelkopf.toSingleEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.ResponseError
import org.springframework.graphql.test.tester.HttpGraphQlTester
import org.springframework.test.context.TestPropertySource
import java.util.*
import kotlin.io.encoding.ExperimentalEncodingApi

@TestPropertySource(
    properties = [
        // Due to the bruteforce nature of this test, it writes a lot of logging when debug mode is active.
        // To prevent this from overwhelming the log receiving side, we run this test explicitly with INFO level.
        "logging.level.game.doppelkopf=INFO",
        "logging.level.root=INFO"
    ]
)
class BruteforceGraphQLPlayTest : BaseGraphQLTest(), Logging {
    private val log = logger()

    @Autowired
    private lateinit var jacksonObjectMapper: ObjectMapper

    private lateinit var gqlTesters: List<HttpGraphQlTester>

    private val cards = Deck.create(DeckMode.DIAMONDS).cards.values.map { it.encoded }

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun `playtest a game over graphql with brute force strategy`() {
        gqlTesters =
            (0..3).map {
                initializeHttpGraphQLTester(initializeWebTestClient(testPlayerNames[it], testPlayerPasswords[it]))
            }

        val gameId = `create a new game, join the players and start it`()

        val dealerId = getGameTree(gameId).also { game ->
            log.atDebug()
                .setMessage { "Obtained current game tree information from graphql API." }
                .addKeyValue("gameTree") { jacksonObjectMapper.writeValueAsString(game) }
                .log()

            assertThat(game.players).hasSize(4)
        }.players.single { it.dealer }.user.id

        val initDealerIndex = testPlayers.indexOfFirst { it.id == dealerId }

        // 8 rounds per game, i.e. each player is the dealer 2 times
        repeat(8) {
            val dealerIndex = (initDealerIndex + it) % 4

            val roundId = `deal a new round`(gameId, dealerIndex)

            getRoundTree(roundId).also { round ->
                assertThat(round.number).isEqualTo(it + 1)
                assertThat(round.state).isEqualTo(RoundState.WAITING_FOR_DECLARATIONS)
            }

            `declare and auction the hands`(roundId)

            getGameTree(gameId).also { game ->
                assertThat(game.state).isEqualTo(GameState.PLAYING_ROUND)
            }
            getRoundTree(roundId).also { round ->
                assertThat(round.state).isEqualTo(RoundState.PLAYING_TRICKS)
            }

            repeat(cards.size / 4) { tI ->
                `play cards for the trick`(roundId, tI + 1)
            }

            getRoundTree(roundId).also { round ->
                assertThat(round.state).isEqualTo(RoundState.EVALUATED)
                assertThat(round.tricks).hasSize(cards.size / 4)
                assertThat(round.se.ended).isNotNull
                assertThat(round.result).isNotNull.withFailMessage {
                    "Expected round to be completed and evaluated with result set, but result is null."
                }
            }

            log.atInfo()
                .setMessage { "Finished round in BruteForce strategy." }
                .addKeyValue("gameId", gameId)
                .addKeyValue("roundId", roundId)
                .addKeyValue("number", it)
                .log()
        }
    }

    private fun `create a new game, join the players and start it`(): UUID {
        val response = gqlTesters[0].documentName("createGame")
            .fragName("gameProperties")
            .fragName("cu")
            .fragName("se")
            .variable("playerLimit", 4)
            .execute()
            .toSingleEntity<GameResponse>()

        repeat(3) {
            gqlTesters[it + 1].documentName("joinGame")
                .fragName("playerProperties")
                .fragName("cu")
                .variable("gameId", response.id)
                .variable("seat", it + 1)
                .executeAndVerify()
        }

        gqlTesters[0].documentName("startGame")
            .fragName("gameProperties")
            .fragName("cu")
            .fragName("se")
            .variable("gameId", response.id)
            .executeAndVerify()

        return response.id
    }

    private fun `declare and auction the hands`(roundId: UUID) {
        val round = getRoundTree(roundId)

        val hasReservation: MutableMap<Int, Boolean?> = (0..4).associateWith { null }.toMutableMap()

        repeat(4) { iter ->
            if (`try to declare option`(
                    round.id,
                    round.hands.single { h -> h.player.user.id == testPlayers[iter].id }.id,
                    iter,
                    DeclarationOption.HEALTHY
                )
            ) {
                hasReservation[iter] = false
            } else if (`try to declare option`(
                    round.id,
                    round.hands.single { h -> h.player.user.id == testPlayers[iter].id }.id,
                    iter,
                    DeclarationOption.RESERVATION
                )
            ) {
                hasReservation[iter] = true
            }

            assertThat(hasReservation[iter]).isNotNull
                .withFailMessage { "Neither healthy nor reservation could be declared successfully for player # $iter" }
        }

        hasReservation.filter { it.value == true }.forEach {
            val bid = `try to make bid`(
                round.id,
                round.hands.single { h -> h.player.user.id == testPlayers[it.key].id }.id,
                it.key,
                BiddingOption.MARRIAGE
            )

            assertThat(bid).isTrue
                .withFailMessage { "Could not declare marriage on the hand that made the reservation because it could not declare healthy before for player # ${it.key}" }
        }
    }

    private fun `deal a new round`(gameId: UUID, dealerIndex: Int): UUID {
        val response = gqlTesters[dealerIndex].documentName("createRound")
            .fragName("roundProperties")
            .fragName("resultProperties")
            .fragName("sq")
            .fragName("cu")
            .fragName("se")
            .variable("gameId", gameId)
            .execute()
            .toSingleEntity<RoundResponse>()

        return response.id
    }

    private fun `try to declare option`(
        roundId: UUID,
        handId: UUID,
        playerIndex: Int,
        declarationOption: DeclarationOption,
    ): Boolean {
        return runCatching {
            gqlTesters[playerIndex].documentName("declareHand")
                .fragName("privateHandProperties")
                .fragName("publicHandProperties")
                .fragName("cu")
                .variable("handId", handId)
                .variable("declaration", declarationOption)
                .executeAndVerify()
        }.onFailure {
            log.atDebug()
                .setMessage { "Could not make declaration." }
                .addKeyValue("playerIndex", playerIndex)
                .addKeyValue("roundId", roundId)
                .addKeyValue("handId", handId)
                .addKeyValue("declaration", declarationOption)
                .setCause(it)
                .log()
        }.isSuccess
    }

    private fun `try to make bid`(
        roundId: UUID,
        handId: UUID,
        playerIndex: Int,
        biddingOption: BiddingOption
    ): Boolean {
        return runCatching {
            gqlTesters[playerIndex].documentName("bidHand")
                .fragName("privateHandProperties")
                .fragName("publicHandProperties")
                .fragName("cu")
                .variable("handId", handId)
                .variable("bidding", biddingOption)
                .executeAndVerify()
        }.onFailure {
            log.atDebug()
                .setMessage { "Could not make bid." }
                .addKeyValue("playerIndex", playerIndex)
                .addKeyValue("roundId", roundId)
                .addKeyValue("handId", handId)
                .addKeyValue("bid", biddingOption)
                .setCause(it)
                .log()
        }.isSuccess
    }

    private fun `play cards for the trick`(roundId: UUID, expectedTrickNumber: Int) {
        var playerIndex = 0
        do {
            val played = `bruteforce the next card to play`(roundId, playerIndex)
            if (!played) {
                playerIndex++
            }
        } while (!played && playerIndex < 4)

        // We should have the index of the player that played the first card here.
        // If this is 4, we could not play the first card with any of our testPlayers.
        assertThat(playerIndex).isLessThan(4)

        repeat(3) {
            // The next 3 players are playing in their seating order behind the one that started.
            val played = `bruteforce the next card to play`(roundId, (playerIndex + it + 1) % 4)
            assertThat(played).isTrue
        }

        // 4 cards played, the trick is complete

        getRoundTree(roundId).also { round ->
            round.currentTrick!!.also { curTrick ->
                assertThat(round.tricks.maxBy { it.number }.id).isEqualTo(curTrick.id)

                assertThat(curTrick.number).isEqualTo(expectedTrickNumber)
                assertThat(curTrick.winner).isNotNull
                assertThat(curTrick.se.ended).isNotNull
                assertThat(curTrick.cards).hasSize(4)
            }
        }
    }

    private fun `bruteforce the next card to play`(roundId: UUID, playingIndex: Int): Boolean {
        cards.forEach { card ->
            var errors: List<ResponseError>? = null
            gqlTesters[playingIndex].documentName("playCard")
                .fragName("turnProperties")
                .fragName("cu")
                .variable("roundId", roundId)
                .variable("card", card)
                .execute()
                .errors()
                .satisfy {
                    // capture the errors via writing to the error var defined above
                    errors = it
                }

            if (errors == null) {
                fail { "Could not extract the error list from the gql card play response for ${roundId}, player $playingIndex and $card." }
            }

            if (errors.isEmpty()) {
                // no errors, we consider the card to be played successfully
                return true
            }

            log.atDebug()
                .setMessage { "Failed to determine the correct player." }
                .addKeyValue("playingIndex", playingIndex)
                .addKeyValue("roundId", roundId)
                .log()
        }

        return false
    }
}