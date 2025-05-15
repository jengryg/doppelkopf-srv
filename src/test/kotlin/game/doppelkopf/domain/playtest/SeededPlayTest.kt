package game.doppelkopf.domain.playtest

import game.doppelkopf.BaseSpringBootTest
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.domain.deck.model.Deck
import game.doppelkopf.domain.game.GameEngine
import game.doppelkopf.domain.game.enums.GameState
import game.doppelkopf.domain.game.ports.commands.GameCommandDealNewRound
import game.doppelkopf.domain.game.ports.commands.GameCommandJoinAsPlayer
import game.doppelkopf.domain.game.ports.commands.GameCommandStartPlaying
import game.doppelkopf.domain.hand.HandEngine
import game.doppelkopf.domain.hand.enums.DeclarationOption
import game.doppelkopf.domain.hand.enums.DefiniteTeam
import game.doppelkopf.domain.hand.ports.commands.HandCommandDeclare
import game.doppelkopf.domain.lobby.LobbyEngine
import game.doppelkopf.domain.lobby.ports.commands.LobbyCommandCreateNewGame
import game.doppelkopf.domain.round.RoundEngine
import game.doppelkopf.domain.round.ports.commands.RoundCommandPlayCard
import game.doppelkopf.domain.trick.enums.TrickState
import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class SeededPlayTest : BaseSpringBootTest(), Logging {
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
            .setMessage { "Running Seeded strategy for game." }
            .addKeyValue("seed") { Base64.UrlSafe.withPadding(Base64.PaddingOption.PRESENT).encode(game.seed) }
            .log()

        assertThat(game.state).isEqualTo(GameState.WAITING_FOR_DEAL)
        // seed should have player in seat 3 as dealer
        assertThat(game.players.single { it.seat == 3 }.dealer).isTrue

        val round = `deal a new round, declare and auction the hands`(game, 3)

        `play the game according to a pre-determined way`(round)

        assertThat(round.results).hasSize(2)

        assertThat(round.hands.single { it.player.user.id == testPlayers[0].id }.score).isEqualTo(54)
        assertThat(round.hands.single { it.player.user.id == testPlayers[1].id }.score).isEqualTo(115)
        assertThat(round.hands.single { it.player.user.id == testPlayers[2].id }.score).isEqualTo(36)
        assertThat(round.hands.single { it.player.user.id == testPlayers[3].id }.score).isEqualTo(35)

        round.results.single { it.team == DefiniteTeam.RE }.also {
            assertThat(it.trickCount).isEqualTo(7)
            assertThat(it.score).isEqualTo(150)
            assertThat(it.pointsForWinning).isEqualTo(1)
            assertThat(it.pointsForOpposition).isEqualTo(0)

            assertThat(it.pointsLostScore90).isEqualTo(0)
            assertThat(it.pointsLostScore60).isEqualTo(0)
            assertThat(it.pointsLostScore30).isEqualTo(0)
            assertThat(it.pointsLostScore00).isEqualTo(0)

            assertThat(it.pointsForDoppelkopf).isEqualTo(0)
            assertThat(it.pointsForCharly).isEqualTo(1)
        }

        round.results.single { it.team == DefiniteTeam.KO }.also {
            assertThat(it.trickCount).isEqualTo(5)
            assertThat(it.score).isEqualTo(90)
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
                seed = ByteArray(256) { 0x42 },
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

        repeat(4) { index ->
            handEngine.execute(
                HandCommandDeclare(
                    user = testPlayers[index],
                    hand = round.hands.single({ it.player.user.id == testPlayers[index].id }),
                    declarationOption = DeclarationOption.HEALTHY
                )
            )
        }

        return round
    }

    private fun `play the game according to a pre-determined way`(round: RoundEntity) {
        // cards in hand are based on the seed

        play(round, 0, "AC0")
        play(round, 1, "9C0")
        play(round, 2, "TC0")
        play(round, 3, "KC1")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[0].id)
            assertThat(it.cards).containsExactly("AC0", "9C0", "TC0", "KC1")
            assertThat(it.score).isEqualTo(25)
        }

        play(round, 0, "AH0")
        play(round, 1, "QC1")
        play(round, 2, "9H0")
        play(round, 3, "AH1")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[1].id)
            assertThat(it.cards).containsExactly("AH0", "QC1", "9H0", "AH1")
            assertThat(it.score).isEqualTo(25)
        }

        play(round, 1, "AS1")
        play(round, 2, "9S0")
        play(round, 3, "KS0")
        play(round, 0, "KS1")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[1].id)
            assertThat(it.cards).containsExactly("AS1", "9S0", "KS0", "KS1")
            assertThat(it.score).isEqualTo(19)
        }

        play(round, 1, "9D1")
        play(round, 2, "QH0")
        play(round, 3, "9D0")
        play(round, 0, "JD0")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[2].id)
            assertThat(it.cards).containsExactly("9D1", "QH0", "9D0", "JD0")
            assertThat(it.score).isEqualTo(5)
        }

        play(round, 2, "AS0")
        play(round, 3, "9S1")
        play(round, 0, "TS1")
        play(round, 1, "TS0")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[2].id)
            assertThat(it.cards).containsExactly("AS0", "9S1", "TS1", "TS0")
            assertThat(it.score).isEqualTo(31)
        }

        play(round, 2, "QD0")
        play(round, 3, "QC0")
        play(round, 0, "TH0")
        play(round, 1, "JH1")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[0].id)
            assertThat(it.cards).containsExactly("QD0", "QC0", "TH0", "JH1")
            assertThat(it.score).isEqualTo(18)
        }

        play(round, 0, "AC1")
        play(round, 1, "QS0")
        play(round, 2, "KH0")
        play(round, 3, "TD1")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[1].id)
            assertThat(it.cards).containsExactly("AC1", "QS0", "KH0", "TD1")
            assertThat(it.score).isEqualTo(28)
        }

        play(round, 1, "JC0")
        play(round, 2, "KD0")
        play(round, 3, "JH0")
        play(round, 0, "QD1")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[0].id)
            assertThat(it.cards).containsExactly("JC0", "KD0", "JH0", "QD1")
            assertThat(it.score).isEqualTo(11)
        }

        play(round, 0, "9C1")
        play(round, 1, "JS0")
        play(round, 2, "KD1")
        play(round, 3, "KH1")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[1].id)
            assertThat(it.cards).containsExactly("9C1", "JS0", "KD1", "KH1")
            assertThat(it.score).isEqualTo(10)
        }

        play(round, 1, "QS1")
        play(round, 2, "JD1")
        play(round, 3, "JS1")
        play(round, 0, "QH1")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[1].id)
            assertThat(it.cards).containsExactly("QS1", "JD1", "JS1", "QH1")
            assertThat(it.score).isEqualTo(10)
        }

        play(round, 1, "AD0")
        play(round, 2, "TD0")
        play(round, 3, "TH1")
        play(round, 0, "KC0")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[3].id)
            assertThat(it.cards).containsExactly("AD0", "TD0", "TH1", "KC0")
            assertThat(it.score).isEqualTo(35)
        }

        play(round, 3, "9H1")
        play(round, 0, "TC1")
        play(round, 1, "JC1")
        play(round, 2, "AD1")

        round.tricks.maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.turns).hasSize(4)
            assertThat(it.winner?.player?.user?.id).isEqualTo(testPlayers[1].id)
            assertThat(it.cards).containsExactly("9H1", "TC1", "JC1", "AD1")
            assertThat(it.score).isEqualTo(23)
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