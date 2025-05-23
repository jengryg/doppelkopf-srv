package game.doppelkopf.domain.playtest

import game.doppelkopf.BaseSpringBootTest
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
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
import game.doppelkopf.domain.hand.enums.DefiniteTeam
import game.doppelkopf.domain.hand.ports.commands.HandCommandBid
import game.doppelkopf.domain.hand.ports.commands.HandCommandDeclare
import game.doppelkopf.domain.lobby.LobbyEngine
import game.doppelkopf.domain.lobby.ports.commands.LobbyCommandCreateNewGame
import game.doppelkopf.domain.round.RoundEngine
import game.doppelkopf.domain.round.enums.RoundContract
import game.doppelkopf.domain.round.enums.RoundState
import game.doppelkopf.domain.round.ports.commands.RoundCommandPlayCard
import game.doppelkopf.domain.trick.enums.TrickState
import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class SeededSoloPlayTest : BaseSpringBootTest(), Logging {
    private val log = logger()

    @Autowired
    private lateinit var lobbyEngine: LobbyEngine

    @Autowired
    private lateinit var gameEngine: GameEngine

    @Autowired
    private lateinit var roundEngine: RoundEngine

    @Autowired
    private lateinit var handEngine: HandEngine

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun `playtest a game with seed and implemented strategy`() {
        val game = `create a new game using a seed, join the players and start it`()

        log.atInfo()
            .setMessage { "Running Seeded strategy for game with solo play." }
            .addKeyValue("seed") { Base64.UrlSafe.withPadding(Base64.PaddingOption.PRESENT).encode(game.seed) }
            .log()

        assertThat(game.state).isEqualTo(GameState.WAITING_FOR_DEAL)
        // seed should have player in seat 3 as dealer
        assertThat(game.players.single { it.seat == 3 }.dealer).isTrue

        val round = `deal a new round, declare and auction the hands`(game, 3)

        `play the game according to a pre-determined way`(round)

        assertThat(round.results).hasSize(2)

        assertThat(round.hands.single { it.player.user.id == testPlayers[0].id }.score).isEqualTo(15)
        assertThat(round.hands.single { it.player.user.id == testPlayers[1].id }.score).isEqualTo(69)
        assertThat(round.hands.single { it.player.user.id == testPlayers[2].id }.score).isEqualTo(138)
        assertThat(round.hands.single { it.player.user.id == testPlayers[3].id }.score).isEqualTo(18)

        round.results.single { it.team == DefiniteTeam.RE }.also {
            assertThat(it.trickCount).isEqualTo(7)
            assertThat(it.score).isEqualTo(138)
            assertThat(it.pointsForWinning).isEqualTo(1)
            assertThat(it.pointsForOpposition).isEqualTo(0)

            assertThat(it.pointsLostScore90).isEqualTo(0)
            assertThat(it.pointsLostScore60).isEqualTo(0)
            assertThat(it.pointsLostScore30).isEqualTo(0)
            assertThat(it.pointsLostScore00).isEqualTo(0)

            assertThat(it.pointsForDoppelkopf).isEqualTo(0)
            assertThat(it.pointsForCharly).isEqualTo(0)
        }

        round.results.single { it.team == DefiniteTeam.KO }.also {
            assertThat(it.trickCount).isEqualTo(5)
            assertThat(it.score).isEqualTo(102)
            assertThat(it.pointsForWinning).isEqualTo(0)
            assertThat(it.pointsForOpposition).isEqualTo(0)

            assertThat(it.pointsLostScore90).isEqualTo(0)
            assertThat(it.pointsLostScore60).isEqualTo(0)
            assertThat(it.pointsLostScore30).isEqualTo(0)
            assertThat(it.pointsLostScore00).isEqualTo(0)

            assertThat(it.pointsForDoppelkopf).isEqualTo(0)
            assertThat(it.pointsForCharly).isEqualTo(0)
        }
    }

    private fun `create a new game using a seed, join the players and start it`(): GameEntity {
        val game = lobbyEngine.execute(
            LobbyCommandCreateNewGame(
                user = testPlayers[0],
                playerLimit = 4,
                seed = ByteArray(256) { 0x17 },
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

        repeat(2) { index ->
            handEngine.execute(
                HandCommandDeclare(
                    user = testPlayers[index],
                    hand = round.hands.single { it.player.user.id == testPlayers[index].id },
                    declarationOption = DeclarationOption.HEALTHY
                )
            )
        }

        handEngine.execute(
            HandCommandDeclare(
                user = testPlayers[2],
                hand = round.hands.single { it.player.user.id == testPlayers[2].id },
                declarationOption = DeclarationOption.RESERVATION
            )
        )

        handEngine.execute(
            HandCommandDeclare(
                user = testPlayers[3],
                hand = round.hands.single { it.player.user.id == testPlayers[3].id },
                declarationOption = DeclarationOption.RESERVATION
            )
        )

        assertThat(round.state).isEqualTo(RoundState.WAITING_FOR_BIDS)

        handEngine.execute(
            HandCommandBid(
                user = testPlayers[2],
                hand = round.hands.single { it.player.user.id == testPlayers[2].id },
                biddingOption = BiddingOption.SOLO_CLUBS
            )
        )

        handEngine.execute(
            HandCommandBid(
                user = testPlayers[3],
                hand = round.hands.single { it.player.user.id == testPlayers[3].id },
                biddingOption = BiddingOption.SOLO_FREE
            )
        )

        // player with index 2 wins the bid to play a SOLO of Clubs

        assertThat(round.contract).isEqualTo(RoundContract.SOLO)
        assertThat(round.deckMode).isEqualTo(DeckMode.CLUBS)

        return round
    }

    private fun `play the game according to a pre-determined way`(round: RoundEntity) {
        // cards in hand are based on the seed

        play(round, 0, "AD0")
        play(round, 1, "AD1")
        play(round, 2, "9C0")
        play(round, 3, "KD1")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[2].id)
            assertThat(it.cards).containsExactly("AD0", "AD1", "9C0", "KD1")
            assertThat(it.score).isEqualTo(26)
        }

        play(round, 2, "AS0")
        play(round, 3, "KS0")
        play(round, 0, "KS1")
        play(round, 1, "9S0")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[2].id)
            assertThat(it.cards).containsExactly("AS0", "KS0", "KS1", "9S0")
            assertThat(it.score).isEqualTo(19)
        }

        play(round, 2, "AH1")
        play(round, 3, "9H0")
        play(round, 0, "9H1")
        play(round, 1, "JH0")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[1].id)
            assertThat(it.cards).containsExactly("AH1", "9H0", "9H1", "JH0")
            assertThat(it.score).isEqualTo(13)
        }

        play(round, 1, "TH0")
        play(round, 2, "JD1")
        play(round, 3, "TC0")
        play(round, 0, "KC1")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[1].id)
            assertThat(it.cards).containsExactly("TH0", "JD1", "TC0", "KC1")
            assertThat(it.score).isEqualTo(26)
        }

        play(round, 1, "9D1")
        play(round, 2, "QC1")
        play(round, 3, "KC0")
        play(round, 0, "9D0")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[2].id)
            assertThat(it.cards).containsExactly("9D1", "QC1", "KC0", "9D0")
            assertThat(it.score).isEqualTo(7)
        }

        play(round, 2, "9C1")
        play(round, 3, "JD0")
        play(round, 0, "JC1")
        play(round, 1, "AC0")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[0].id)
            assertThat(it.cards).containsExactly("9C1", "JD0", "JC1", "AC0")
            assertThat(it.score).isEqualTo(15)
        }

        play(round, 0, "KH0")
        play(round, 1, "AC1")
        play(round, 2, "KH1")
        play(round, 3, "AH0")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[1].id)
            assertThat(it.cards).containsExactly("KH0", "AC1", "KH1", "AH0")
            assertThat(it.score).isEqualTo(30)
        }

        play(round, 1, "QC0")
        play(round, 2, "TH1")
        play(round, 3, "JS0")
        play(round, 0, "QD0")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[2].id)
            assertThat(it.cards).containsExactly("QC0", "TH1", "JS0", "QD0")
            assertThat(it.score).isEqualTo(18)
        }

        play(round, 2, "QS0")
        play(round, 3, "JS1")
        play(round, 0, "KD0")
        play(round, 1, "TC1")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[2].id)
            assertThat(it.cards).containsExactly("QS0", "JS1", "KD0", "TC1")
            assertThat(it.score).isEqualTo(19)
        }

        play(round, 2, "JH1")
        play(round, 3, "QS1")
        play(round, 0, "TD0")
        play(round, 1, "QD1")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[3].id)
            assertThat(it.cards).containsExactly("JH1", "QS1", "TD0", "QD1")
            assertThat(it.score).isEqualTo(18)
        }

        play(round, 3, "AS1")
        play(round, 0, "TS1")
        play(round, 1, "9S1")
        play(round, 2, "JC0")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[2].id)
            assertThat(it.cards).containsExactly("AS1", "TS1", "9S1", "JC0")
            assertThat(it.score).isEqualTo(23)
        }

        play(round, 2, "QH0")
        play(round, 3, "TS0")
        play(round, 0, "TD1")
        play(round, 1, "QH1")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[2].id)
            assertThat(it.cards).containsExactly("QH0", "TS0", "TD1", "QH1")
            assertThat(it.score).isEqualTo(26)
        }
    }

    private fun play(round: RoundEntity, playerIndex: Int, encCard: String) {
        roundEngine.execute(
            RoundCommandPlayCard(
                testPlayers[playerIndex],
                round,
                Deck.create(round.deckMode).getCard(encCard).getOrThrow()
            )
        )
    }
}