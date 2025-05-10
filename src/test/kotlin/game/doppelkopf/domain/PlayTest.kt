package game.doppelkopf.domain

import game.doppelkopf.BaseSpringBootTest
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.domain.deck.enums.DeckMode
import game.doppelkopf.domain.deck.model.Deck
import game.doppelkopf.domain.game.GameEngine
import game.doppelkopf.domain.game.enums.GameState
import game.doppelkopf.domain.game.ports.commands.GameCommandDealNewRound
import game.doppelkopf.domain.game.ports.commands.GameCommandJoinAsPlayer
import game.doppelkopf.domain.game.ports.commands.GameCommandStartPlaying
import game.doppelkopf.domain.hand.HandEngine
import game.doppelkopf.domain.hand.enums.BiddingOption
import game.doppelkopf.domain.hand.enums.DeclarationOption
import game.doppelkopf.domain.hand.ports.commands.HandCommandBid
import game.doppelkopf.domain.hand.ports.commands.HandCommandDeclare
import game.doppelkopf.domain.lobby.LobbyEngine
import game.doppelkopf.domain.lobby.ports.commands.LobbyCommandCreateNewGame
import game.doppelkopf.domain.round.RoundEngine
import game.doppelkopf.domain.round.enums.RoundState
import game.doppelkopf.domain.round.ports.commands.RoundCommandPlayCard
import game.doppelkopf.domain.trick.enums.TrickState
import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class PlayTest : BaseSpringBootTest(), Logging {
    private val log = logger()

    @Autowired
    private lateinit var lobbyEngine: LobbyEngine

    @Autowired
    private lateinit var gameEngine: GameEngine

    @Autowired
    private lateinit var RoundEngine: RoundEngine

    @Autowired
    private lateinit var handEngine: HandEngine

    val cards = Deck.create(DeckMode.DIAMONDS).cards.values.map { it.encoded }

    @Test
    fun `playtest a game with brute force strategy`() {
        val game = `create a new game, join the players and start it`()

        assertThat(game.state).isEqualTo(GameState.WAITING_FOR_DEAL)

        val initDealerIndex = game.players.single { it.dealer }.user.let { testPlayers.indexOf(it) }

        // 8 rounds per game, i.e. each player is the dealer 2 times
        repeat(8) {
            val dealerIndex = (initDealerIndex + it) % 4

            val round = `deal a new round, declare and auction the hands`(game, dealerIndex)

            assertThat(game.state).isEqualTo(GameState.PLAYING_ROUND)

            repeat(cards.size / 4) { tI ->
                `play cards for the trick`(round, tI + 1)
            }

            assertThat(round.ended).isNotNull
            assertThat(round.state).isEqualTo(RoundState.EVALUATED)
            assertThat(round.results).hasSize(2).withFailMessage {
                "Expected round to be complete and evaluated with 2 results."
            }

            log.atInfo()
                .setMessage { "Finished round in bruteforce strategy." }
                .addKeyValue("game", game)
                .addKeyValue("round", round)
                .addKeyValue("number", it)
                .log()
        }
    }

    private fun `create a new game, join the players and start it`(): GameEntity {
        val game =
            lobbyEngine.execute(
                LobbyCommandCreateNewGame(
                    user = testPlayers[0],
                    playerLimit = 4
                )
            )

        repeat(3) {
            gameEngine.execute(
                GameCommandJoinAsPlayer(
                    user = testPlayers[it + 1],
                    game = game,
                    seat = it + 1
                )
            )
        }

        gameEngine.execute(
            GameCommandStartPlaying(
                user = testPlayers[0],
                game = game
            )
        )

        return game
    }

    private fun `deal a new round, declare and auction the hands`(game: GameEntity, dealerIndex: Int): RoundEntity {
        val round =
            gameEngine.execute(
                GameCommandDealNewRound(
                    user = testPlayers[dealerIndex],
                    game = game
                )
            )

        val hasReservation: MutableMap<Int, Boolean?> = (0..4).associateWith { null }.toMutableMap()

        repeat(4) {
            if (`try to declare option`(round, testPlayers[it], DeclarationOption.HEALTHY)) {
                hasReservation[it] = false
            } else if (`try to declare option`(round, testPlayers[it], DeclarationOption.RESERVATION)) {
                hasReservation[it] = true
            }

            assertThat(hasReservation[it]).isNotNull
                .withFailMessage { "Neither healthy nor reservation could be declared successfully for player # $it" }

        }

        hasReservation.filter { it.value == true }.forEach {
            val bid = `try to make bid`(round, testPlayers[it.key], BiddingOption.MARRIAGE)

            assertThat(bid).isTrue
                .withFailMessage { "Could not declare marriage on the hand that made the reservation because it could not declare healthy before for player # ${it.key}" }
        }

        return round
    }

    private fun `try to declare option`(
        round: RoundEntity,
        user: UserEntity,
        declarationOption: DeclarationOption
    ): Boolean {
        return runCatching {
            handEngine.execute(
                HandCommandDeclare(
                    user = user,
                    hand = round.hands.single { it.player.user.id == user.id },
                    declarationOption = declarationOption
                )
            )
        }.onFailure {
            log.atDebug()
                .setMessage { "Could not make bid." }
                .addKeyValue("user", user)
                .addKeyValue("round", round)
                .addKeyValue("declaration", declarationOption)
                .setCause(it)
                .log()
        }.isSuccess
    }

    private fun `try to make bid`(
        round: RoundEntity,
        user: UserEntity,
        biddingOption: BiddingOption
    ): Boolean {
        return runCatching {
            handEngine.execute(
                HandCommandBid(
                    user = user,
                    hand = round.hands.single { it.player.user.id == user.id },
                    biddingOption = biddingOption
                )
            )
        }.onFailure {
            log.atDebug()
                .setMessage { "Could not make bid." }
                .addKeyValue("user", user)
                .addKeyValue("round", round)
                .addKeyValue("bid", biddingOption)
                .setCause(it)
                .log()
        }.isSuccess
    }

    private fun `play cards for the trick`(round: RoundEntity, expectedTrickNumber: Int) {
        var playerIndex = 0
        do {
            val played = `bruteforce the next card to play`(round, testPlayers[playerIndex])
            if(!played) {
                playerIndex++
            }

        } while (!played && playerIndex < 4)

        // We should have the index of the player that played the first card here.
        // If this is 4, we could not play the first card with any of our testPlayers.
        assertThat(playerIndex).isLessThan(4)

        println("Card 0 in trick $expectedTrickNumber was played by index $playerIndex")

        repeat(3) {
            // The next 3 players are playing in their seating order behind the one that started.
            val played = `bruteforce the next card to play`(round, testPlayers[(playerIndex + it + 1) % 4])
            println("Card ${it + 1} in trick $expectedTrickNumber was played: $played")
            assertThat(played).isTrue
        }

        // 4 cards played, the trick is complete

        round.tricks.maxBy { it.number }.also { finishedTrick ->
            assertThat(finishedTrick.number).isEqualTo(expectedTrickNumber)
            assertThat(finishedTrick.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(finishedTrick.winner).isNotNull
            assertThat(finishedTrick.ended).isNotNull
        }
    }

    private fun `bruteforce the next card to play`(round: RoundEntity, user: UserEntity): Boolean {
        cards.forEach { card ->
            val result = runCatching {
                RoundEngine.execute(
                    RoundCommandPlayCard(
                        user,
                        round,
                        Deck.create(round.deckMode).getCard(card).getOrThrow()
                    )
                )
            }

            if (result.isSuccess) {
                println("Card played by ${user.username}: $card remaining cards in hand: ${round.hands.single { it.player.user.id == user.id }.cardsRemaining}")
                return true
            }
        }

        return false
    }
}