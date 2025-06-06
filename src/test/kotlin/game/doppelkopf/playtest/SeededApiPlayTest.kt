package game.doppelkopf.playtest

import game.doppelkopf.BaseRestAssuredTest
import game.doppelkopf.adapter.api.core.game.dto.GameCreateDto
import game.doppelkopf.adapter.api.core.game.dto.GameInfoDto
import game.doppelkopf.adapter.api.core.game.dto.GameOperationDto
import game.doppelkopf.adapter.api.core.hand.dto.DeclarationCreateDto
import game.doppelkopf.adapter.api.core.hand.dto.HandForPlayerDto
import game.doppelkopf.adapter.api.core.hand.dto.HandPublicInfoDto
import game.doppelkopf.adapter.api.core.player.dto.PlayerCreateDto
import game.doppelkopf.adapter.api.core.player.dto.PlayerInfoDto
import game.doppelkopf.adapter.api.core.round.dto.RoundInfoDto
import game.doppelkopf.adapter.api.core.trick.dto.TrickInfoDto
import game.doppelkopf.adapter.api.core.turn.dto.CreateTurnDto
import game.doppelkopf.adapter.api.core.turn.dto.TurnInfoDto
import game.doppelkopf.domain.game.enums.GameOperation
import game.doppelkopf.domain.game.enums.GameState
import game.doppelkopf.domain.hand.enums.DeclarationOption
import game.doppelkopf.domain.hand.enums.DefiniteTeam
import game.doppelkopf.domain.round.enums.RoundState
import game.doppelkopf.domain.trick.enums.TrickState
import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.io.encoding.ExperimentalEncodingApi

class SeededApiPlayTest : BaseRestAssuredTest(), Logging {
    private val log = logger()

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun `playtest a game over api with seed and implemented strategy`() {
        val gameId = `create a new game using a seed, join the players and start it`()

        val players = getResourceList<PlayerInfoDto>(
            path = "/v1/games/$gameId/players",
            expectedStatus = 200
        )

        assertThat(players).hasSize(4)

        val dealerId = players.single { it.dealer }.user.id

        assertThat(dealerId).isEqualTo(testPlayers[3].id)

        val dealerIndex = testPlayers.indexOfFirst { it.id == dealerId }

        val roundId = `deal a new round`(gameId, dealerIndex)

        getResource<RoundInfoDto>(
            path = "/v1/rounds/$roundId",
            expectedStatus = 200
        ).also { round ->
            assertThat(round.number).isEqualTo(1)
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

        val userIdToPlayerId =
            testPlayers.slice(0..3).associate { u -> u.id to players.single { p -> p.user.id == u.id }.id }

        `play the game according to a pre-determined way`(roundId, userIdToPlayerId)

        val round = getResource<RoundInfoDto>(
            path = "/v1/rounds/$roundId",
            expectedStatus = 200
        )

        assertThat(round.state).isEqualTo(RoundState.EVALUATED)
        assertThat(round.result).isNotNull

        round.result!!

        round.result.re.also {
            assertThat(it.team).isEqualTo(DefiniteTeam.RE)
            assertThat(it.tricksCount).isEqualTo(7)
            assertThat(it.score).isEqualTo(150)
            assertThat(it.targetScore).isEqualTo(121)
            assertThat(it.pointsBasic.winning).isEqualTo(1)
            assertThat(it.pointsForSpecial.opposition).isEqualTo(0)

            assertThat(it.pointsLostScore.p90).isEqualTo(0)
            assertThat(it.pointsLostScore.p60).isEqualTo(0)
            assertThat(it.pointsLostScore.p30).isEqualTo(0)
            assertThat(it.pointsLostScore.p00).isEqualTo(0)

            assertThat(it.pointsForSpecial.doppelkopf).isEqualTo(0)
            assertThat(it.pointsForSpecial.charly).isEqualTo(1)
        }

        round.result.ko.also {
            assertThat(it.team).isEqualTo(DefiniteTeam.KO)
            assertThat(it.tricksCount).isEqualTo(5)
            assertThat(it.score).isEqualTo(90)
            assertThat(it.targetScore).isEqualTo(120)
            assertThat(it.pointsBasic.winning).isEqualTo(0)
            assertThat(it.pointsForSpecial.opposition).isEqualTo(0)

            assertThat(it.pointsLostScore.p90).isEqualTo(0)
            assertThat(it.pointsLostScore.p60).isEqualTo(0)
            assertThat(it.pointsLostScore.p30).isEqualTo(0)
            assertThat(it.pointsLostScore.p00).isEqualTo(0)

            assertThat(it.pointsForSpecial.doppelkopf).isEqualTo(0)
            assertThat(it.pointsForSpecial.charly).isEqualTo(0)
        }
    }

    private fun `create a new game using a seed, join the players and start it`(): UUID {
        val (response, _) = createResource<GameCreateDto, GameInfoDto>(
            path = "/v1/games",
            body = GameCreateDto(
                playerLimit = 4,
                seed = ByteArray(256) { 0x42 }
            ),
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

    private fun `deal a new round`(gameId: UUID, dealerIndex: Int): UUID {
        val (response, _) = createResource<RoundInfoDto>(
            path = "/v1/games/$gameId/rounds",
            expectedStatus = 201,
            login = Login(username = testPlayerNames[dealerIndex], testPlayerPasswords[dealerIndex])
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

        repeat(4) { index ->
            val playerIdToFind = players.single { p -> p.user.id == testPlayers[index].id }.id

            createResource<DeclarationCreateDto, HandForPlayerDto>(
                path = "/v1/hands/${hands.single { it.playerId == playerIdToFind }.id}/declarations",
                body = DeclarationCreateDto(
                    declaration = DeclarationOption.HEALTHY
                ),
                expectedStatus = 201,
                login = Login(username = testPlayerNames[index], testPlayerPasswords[index])
            )
        }
    }

    private fun `play the game according to a pre-determined way`(roundId: UUID, userIdToPlayerId: Map<UUID, UUID>) {
        // cards in hand are based on the seed

        play(roundId, 0, "AC0")
        play(roundId, 1, "9C0")
        play(roundId, 2, "TC0")
        play(roundId, 3, "KC1")

        getTricks(roundId).maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.winner?.playerId).isEqualTo(userIdToPlayerId[testPlayers[0].id])
            assertThat(it.cards).containsExactly("AC0", "9C0", "TC0", "KC1")
            assertThat(it.score).isEqualTo(25)
        }

        play(roundId, 0, "AH0")
        play(roundId, 1, "QC1")
        play(roundId, 2, "9H0")
        play(roundId, 3, "AH1")

        getTricks(roundId).maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.winner?.playerId).isEqualTo(userIdToPlayerId[testPlayers[1].id])
            assertThat(it.cards).containsExactly("AH0", "QC1", "9H0", "AH1")
            assertThat(it.score).isEqualTo(25)
        }

        play(roundId, 1, "AS1")
        play(roundId, 2, "9S0")
        play(roundId, 3, "KS0")
        play(roundId, 0, "KS1")

        getTricks(roundId).maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.winner?.playerId).isEqualTo(userIdToPlayerId[testPlayers[1].id])
            assertThat(it.cards).containsExactly("AS1", "9S0", "KS0", "KS1")
            assertThat(it.score).isEqualTo(19)
        }

        play(roundId, 1, "9D1")
        play(roundId, 2, "QH0")
        play(roundId, 3, "9D0")
        play(roundId, 0, "JD0")

        getTricks(roundId).maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.winner?.playerId).isEqualTo(userIdToPlayerId[testPlayers[2].id])
            assertThat(it.cards).containsExactly("9D1", "QH0", "9D0", "JD0")
            assertThat(it.score).isEqualTo(5)
        }

        play(roundId, 2, "AS0")
        play(roundId, 3, "9S1")
        play(roundId, 0, "TS1")
        play(roundId, 1, "TS0")

        getTricks(roundId).maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.winner?.playerId).isEqualTo(userIdToPlayerId[testPlayers[2].id])
            assertThat(it.cards).containsExactly("AS0", "9S1", "TS1", "TS0")
            assertThat(it.score).isEqualTo(31)
        }

        play(roundId, 2, "QD0")
        play(roundId, 3, "QC0")
        play(roundId, 0, "TH0")
        play(roundId, 1, "JH1")

        getTricks(roundId).maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.winner?.playerId).isEqualTo(userIdToPlayerId[testPlayers[0].id])
            assertThat(it.cards).containsExactly("QD0", "QC0", "TH0", "JH1")
            assertThat(it.score).isEqualTo(18)
        }

        play(roundId, 0, "AC1")
        play(roundId, 1, "QS0")
        play(roundId, 2, "KH0")
        play(roundId, 3, "TD1")

        getTricks(roundId).maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.winner?.playerId).isEqualTo(userIdToPlayerId[testPlayers[1].id])
            assertThat(it.cards).containsExactly("AC1", "QS0", "KH0", "TD1")
            assertThat(it.score).isEqualTo(28)
        }

        play(roundId, 1, "JC0")
        play(roundId, 2, "KD0")
        play(roundId, 3, "JH0")
        play(roundId, 0, "QD1")

        getTricks(roundId).maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.winner?.playerId).isEqualTo(userIdToPlayerId[testPlayers[0].id])
            assertThat(it.cards).containsExactly("JC0", "KD0", "JH0", "QD1")
            assertThat(it.score).isEqualTo(11)
        }

        play(roundId, 0, "9C1")
        play(roundId, 1, "JS0")
        play(roundId, 2, "KD1")
        play(roundId, 3, "KH1")

        getTricks(roundId).maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.winner?.playerId).isEqualTo(userIdToPlayerId[testPlayers[1].id])
            assertThat(it.cards).containsExactly("9C1", "JS0", "KD1", "KH1")
            assertThat(it.score).isEqualTo(10)
        }

        play(roundId, 1, "QS1")
        play(roundId, 2, "JD1")
        play(roundId, 3, "JS1")
        play(roundId, 0, "QH1")

        getTricks(roundId).maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.winner?.playerId).isEqualTo(userIdToPlayerId[testPlayers[1].id])
            assertThat(it.cards).containsExactly("QS1", "JD1", "JS1", "QH1")
            assertThat(it.score).isEqualTo(10)
        }

        play(roundId, 1, "AD0")
        play(roundId, 2, "TD0")
        play(roundId, 3, "TH1")
        play(roundId, 0, "KC0")

        getTricks(roundId).maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.winner?.playerId).isEqualTo(userIdToPlayerId[testPlayers[3].id])
            assertThat(it.cards).containsExactly("AD0", "TD0", "TH1", "KC0")
            assertThat(it.score).isEqualTo(35)
        }

        play(roundId, 3, "9H1")
        play(roundId, 0, "TC1")
        play(roundId, 1, "JC1")
        play(roundId, 2, "AD1")

        getTricks(roundId).maxBy { it.number }.also {
            assertThat(it.state).isEqualTo(TrickState.FOURTH_CARD_PLAYED)
            assertThat(it.ended).isNotNull
            assertThat(it.winner?.playerId).isEqualTo(userIdToPlayerId[testPlayers[1].id])
            assertThat(it.cards).containsExactly("9H1", "TC1", "JC1", "AD1")
            assertThat(it.score).isEqualTo(23)
        }
    }

    fun play(roundId: UUID, playingIndex: Int, card: String) {
        createResource<CreateTurnDto, TurnInfoDto>(
            path = "/v1/rounds/$roundId/turns",
            body = CreateTurnDto(card = card),
            expectedStatus = 201,
            login = Login(username = testPlayerNames[playingIndex], testPlayerPasswords[playingIndex])
        )
    }

    fun getTricks(roundId: UUID): List<TrickInfoDto> {
        return getResourceList<TrickInfoDto>(
            "/v1/rounds/$roundId/tricks",
            expectedStatus = 200
        )
    }
}