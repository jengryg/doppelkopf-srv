package game.doppelkopf.playtest

import game.doppelkopf.BaseRestAssuredTest
import game.doppelkopf.adapter.api.core.game.dto.GameCreateDto
import game.doppelkopf.adapter.api.core.game.dto.GameInfoDto
import game.doppelkopf.adapter.api.core.game.dto.GameOperationDto
import game.doppelkopf.adapter.api.core.hand.dto.BidCreateDto
import game.doppelkopf.adapter.api.core.hand.dto.DeclarationCreateDto
import game.doppelkopf.adapter.api.core.hand.dto.HandForPlayerDto
import game.doppelkopf.adapter.api.core.hand.dto.HandPublicInfoDto
import game.doppelkopf.adapter.api.core.player.dto.PlayerCreateDto
import game.doppelkopf.adapter.api.core.player.dto.PlayerInfoDto
import game.doppelkopf.adapter.api.core.round.dto.RoundInfoDto
import game.doppelkopf.adapter.api.core.trick.dto.TrickInfoDto
import game.doppelkopf.adapter.api.core.turn.dto.CreateTurnDto
import game.doppelkopf.adapter.api.core.turn.dto.TurnInfoDto
import game.doppelkopf.domain.deck.enums.DeckMode
import game.doppelkopf.domain.deck.model.Deck
import game.doppelkopf.domain.game.enums.GameOperation
import game.doppelkopf.domain.game.enums.GameState
import game.doppelkopf.domain.hand.enums.BiddingOption
import game.doppelkopf.domain.hand.enums.DeclarationOption
import game.doppelkopf.domain.round.enums.RoundState
import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource
import java.util.*
import kotlin.io.encoding.ExperimentalEncodingApi

@TestPropertySource(
    properties = [
        // Due to the bruteforce nature of this test, it writes a lot of logging when debug mode is active.
        // To prevent this from overwhelming the log receiving side, we run this test explicitly with ERROR level.
        "logging.level.game.doppelkopf=ERROR",
        "logging.level.root=ERROR"
    ]
)
@Disabled("This test is sometimes really slow due to the vast amount of requests it must make.")
class BruteforceApiPlayTest : BaseRestAssuredTest(disableExtendedLogging = true), Logging {
    private val log = logger()

    private val cards = Deck.create(DeckMode.DIAMONDS).cards.values.map { it.encoded }

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun `playtest a game over api with brute force strategy`() {
        val gameId = `create a new game, join the players and start it`()

        val dealerId = getResourceList<PlayerInfoDto>(
            path = "/v1/games/$gameId/players",
            expectedStatus = 200
        ).also { players ->
            assertThat(players).hasSize(4)
        }.single { it.dealer }.user.id

        val initDealerIndex = testPlayers.indexOfFirst { it.id == dealerId }

        // 8 rounds per game, i.e. each player is the dealer 2 times
        repeat(8) {
            val dealerIndex = (initDealerIndex + it) % 4

            val roundId = `deal a new round`(gameId, dealerIndex)

            getResource<RoundInfoDto>(
                path = "/v1/rounds/$roundId",
                expectedStatus = 200
            ).also { round ->
                assertThat(round.number).isEqualTo(it + 1)
                assertThat(round.state).isEqualTo(RoundState.WAITING_FOR_DECLARATIONS)
            }

            `declare and auction the hands`(roundId)

            getResource<GameInfoDto>(
                path = "/v1/games/$gameId",
                expectedStatus = 200
            ).also { game ->
                assertThat(game.state).isEqualTo(GameState.PLAYING_ROUND)
            }

            getResource<RoundInfoDto>(
                path = "/v1/rounds/$roundId",
                expectedStatus = 200
            ).also { round ->
                assertThat(round.state).isEqualTo(RoundState.PLAYING_TRICKS)
            }

            repeat(cards.size / 4) { tI ->
                `play cards for the trick`(roundId, tI + 1)
            }

            getResource<RoundInfoDto>(
                path = "/v1/rounds/$roundId",
                expectedStatus = 200
            ).also { round ->
                assertThat(round.state).isEqualTo(RoundState.EVALUATED)
                assertThat(round.ended).isNotNull
                assertThat(round.result).isNotNull.withFailMessage {
                    "Expected round to be completed and evaluated with result set, but result is null."
                }
            }

            getResourceList<TrickInfoDto>(
                path = "/v1/rounds/$roundId/tricks",
                expectedStatus = 200
            ).also { tricks ->
                assertThat(tricks).hasSize(cards.size / 4)
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
        val (response, _) = createResource<GameCreateDto, GameInfoDto>(
            path = "/v1/games",
            body = GameCreateDto(playerLimit = 4),
            expectedStatus = 201,
            login = Login(username = testPlayerNames[0], testPlayerPasswords[0])
        )

        repeat(3) {
            createResource<PlayerCreateDto, PlayerInfoDto>(
                path = "/v1/games/${response.id}/players",
                body = PlayerCreateDto(seat = it + 1),
                expectedStatus = 201,
                login = Login(username = testPlayerNames[it + 1], testPlayerPasswords[it + 1])
            )
        }

        patchResource<GameOperationDto, GameInfoDto>(
            path = "/v1/games/${response.id}",
            body = GameOperationDto(
                op = GameOperation.START
            ),
            expectedStatus = 200,
            login = Login(username = testPlayerNames[0], testPlayerPasswords[0])
        )

        return response.id
    }

    private fun `declare and auction the hands`(roundId: UUID) {
        val round = getResource<RoundInfoDto>(
            path = "/v1/rounds/$roundId",
            expectedStatus = 200
        )

        val players = getResourceList<PlayerInfoDto>(
            path = "/v1/games/${round.gameId}/players",
            expectedStatus = 200
        )

        val hands = getResourceList<HandPublicInfoDto>(
            path = "/v1/rounds/$roundId/hands",
            expectedStatus = 200,
        )


        assertThat(hands).hasSize(4)

        val hasReservation: MutableMap<Int, Boolean?> = (0..4).associateWith { null }.toMutableMap()

        repeat(4) { iter ->
            val playerIdToFind = players.single { p -> p.user.id == testPlayers[iter].id }.id

            if (`try to declare option`(
                    roundId,
                    hands.single { h -> h.playerId == playerIdToFind }.id,
                    iter,
                    DeclarationOption.HEALTHY
                )
            ) {
                hasReservation[iter] = false
            } else if (`try to declare option`(
                    roundId,
                    hands.single { h -> h.playerId == playerIdToFind }.id,
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
            val playerIdToFind = players.single { p -> p.user.id == testPlayers[it.key].id }.id

            val bid = `try to make bid`(
                round.id,
                hands.single { h -> h.playerId == playerIdToFind }.id,
                it.key,
                BiddingOption.MARRIAGE
            )

            assertThat(bid).isTrue
                .withFailMessage { "Could not declare marriage on the hand that made the reservation because it could not declare healthy before for player # ${it.key}" }
        }
    }

    private fun `deal a new round`(gameId: UUID, dealerIndex: Int): UUID {
        val (response, _) = createResource<RoundInfoDto>(
            path = "/v1/games/$gameId/rounds",
            expectedStatus = 201,
            login = Login(username = testPlayerNames[dealerIndex], testPlayerPasswords[dealerIndex])
        )

        return response.id
    }

    private fun `try to declare option`(
        roundId: UUID,
        handId: UUID,
        playerIndex: Int,
        declarationOption: DeclarationOption,
    ): Boolean {
        return runCatching {
            createResource<DeclarationCreateDto, HandForPlayerDto>(
                path = "/v1/hands/$handId/declarations",
                body = DeclarationCreateDto(
                    declaration = declarationOption
                ),
                expectedStatus = 201,
                login = Login(username = testPlayerNames[playerIndex], testPlayerPasswords[playerIndex])
            )
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
            createResource<BidCreateDto, HandForPlayerDto>(
                path = "/v1/hands/$handId/bids",
                body = BidCreateDto(
                    bid = biddingOption
                ),
                expectedStatus = 201,
                login = Login(username = testPlayerNames[playerIndex], testPlayerPasswords[playerIndex])
            )
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

        getResource<RoundInfoDto>(
            path = "/v1/rounds/$roundId",
            expectedStatus = 200
        )

        getResourceList<TrickInfoDto>(
            path = "/v1/rounds/$roundId/tricks",
            expectedStatus = 200
        ).also { tricks ->
            tricks.maxBy { it.number }.also { curTrick ->
                assertThat(curTrick.number).isEqualTo(expectedTrickNumber)
                assertThat(curTrick.winner).isNotNull
                assertThat(curTrick.cards).hasSize(4)
            }
        }
    }

    private fun `bruteforce the next card to play`(roundId: UUID, playingIndex: Int): Boolean {
        cards.forEach { card ->
            runCatching {
                createResource<CreateTurnDto, TurnInfoDto>(
                    path = "/v1/rounds/$roundId/turns",
                    body = CreateTurnDto(card = card),
                    expectedStatus = 201,
                    login = Login(username = testPlayerNames[playingIndex], testPlayerPasswords[playingIndex])
                )
            }.onSuccess {
                return true
            }
        }

        log.atDebug()
            .setMessage { "Failed to determine the correct player." }
            .addKeyValue("playingIndex", playingIndex)
            .addKeyValue("roundId", roundId)
            .log()

        return false
    }
}