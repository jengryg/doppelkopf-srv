package game.doppelkopf.adapter.api.core

import game.doppelkopf.BaseRestAssuredTest
import game.doppelkopf.adapter.api.core.result.dto.ResultInfoResponse
import game.doppelkopf.adapter.api.core.result.dto.TeamedResultInfoDto
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.game.GameRepository
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.adapter.persistence.model.player.PlayerRepository
import game.doppelkopf.adapter.persistence.model.result.ResultEntity
import game.doppelkopf.adapter.persistence.model.result.ResultRepository
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.round.RoundRepository
import game.doppelkopf.domain.hand.enums.DefiniteTeam
import game.doppelkopf.errors.ProblemDetailResponse
import game.doppelkopf.utils.Teamed
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test

class ResultControllerTest : BaseRestAssuredTest() {
    @Autowired
    private lateinit var playerRepository: PlayerRepository

    @Autowired
    private lateinit var gameRepository: GameRepository

    @Autowired
    private lateinit var roundRepository: RoundRepository

    @Autowired
    private lateinit var resultRepository: ResultRepository

    @BeforeEach
    @AfterEach
    fun `unmock all`() {
        clearAllMocks()
        unmockkAll()
    }

    @Nested
    inner class GetAndList {
        @Test
        fun `get results of round that has no result returns teamed null`() {
            val round = createPersistedRoundEntity()

            val response =
                getResource<Teamed<ResultInfoResponse?>>(path = "/v1/rounds/${round.id}/results", expectedStatus = 200)

            assertThat(response.re).isNull()
            assertThat(response.ko).isNull()
        }

        @Test
        fun `get results of round with unknown id returns 404`() {
            val response = getResource<ProblemDetailResponse>(path = "/v1/rounds/$zeroId/results", expectedStatus = 404)

            assertThat(response.instance.toString()).isEqualTo("/v1/rounds/$zeroId/results")
            assertThat(response.title).isEqualTo("Entity not found")
            assertThat(response.detail).isEqualTo("The entity of type RoundEntity with id $zeroId was not found.")
        }

        @Test
        fun `get results of round that has results returns 200 with teamed dtos`() {
            val round = createPersistedRoundEntity()
            val resultRe = createResultEntity(round, DefiniteTeam.RE, 17).let {
                resultRepository.save(it)
            }
            val resultKo = createResultEntity(round, DefiniteTeam.KO, 42).let {
                resultRepository.save(it)
            }

            round.results.add(resultRe)
            round.results.add(resultKo)

            val response = getResource<TeamedResultInfoDto>(
                path = "/v1/rounds/${round.id}/results",
                expectedStatus = 200
            )

            response.re.also {
                assertThat(it).isNotNull
                it!!

                assertThat(it.id).isEqualTo(resultRe.id)
                assertThat(it.roundId).isEqualTo(round.id)
                assertThat(it.team).isEqualTo(DefiniteTeam.RE)
            }

            response.ko.also {
                assertThat(it).isNotNull
                it!!

                assertThat(it.id).isEqualTo(resultKo.id)
                assertThat(it.roundId).isEqualTo(round.id)
                assertThat(it.team).isEqualTo(DefiniteTeam.KO)
            }
        }

        @Test
        fun `get specific result with unknown id returns 404`() {
            val response = getResource<ProblemDetailResponse>(
                path = "/v1/results/$zeroId",
                expectedStatus = 404
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/results/$zeroId")
            assertThat(response.title).isEqualTo("Entity not found")
            assertThat(response.detail).isEqualTo("The entity of type ResultEntity with id $zeroId was not found.")
        }

        @Test
        fun `get specific result with known uuid returns 200 and dto`() {
            val result = createResultEntity(
                round = createPersistedRoundEntity(),
                team = DefiniteTeam.RE,
                number = 47
            ).let { resultRepository.save(it) }

            val response = getResource<ResultInfoResponse>(
                path = "/v1/results/${result.id}",
                expectedStatus = 200
            )

            assertThat(response.id).isEqualTo(result.id)
            assertThat(response.roundId).isEqualTo(result.round.id)
            assertThat(response.team).isEqualTo(DefiniteTeam.RE)
        }

        private fun createResultEntity(round: RoundEntity, team: DefiniteTeam, number: Int): ResultEntity {
            return ResultEntity(
                round = round,
                team = team,
                trickCount = number,
                score = number,
                target = number,
                pointsForWinning = number,
                pointsLostScore90 = number,
                pointsLostScore60 = number,
                pointsLostScore30 = number,
                pointsLostScore00 = number,
                pointsBasicCallsRe = number,
                pointsBasicCallsKo = number,
                pointsUnderCallsRe90 = number,
                pointsUnderCallsKo90 = number,
                pointsUnderCallsRe60 = number,
                pointsUnderCallsKo60 = number,
                pointsUnderCallsRe30 = number,
                pointsUnderCallsKo30 = number,
                pointsUnderCallsRe00 = number,
                pointsUnderCallsKo00 = number,
                pointsBeatingRe90 = number,
                pointsBeatingKo90 = number,
                pointsBeatingRe60 = number,
                pointsBeatingKo60 = number,
                pointsBeatingRe30 = number,
                pointsBeatingKo30 = number,
                pointsBeatingRe00 = number,
                pointsBeatingKo00 = number,
                pointsForOpposition = number,
                pointsForDoppelkopf = number,
                pointsForCharly = number,
            )
        }

        private fun createPersistedRoundEntity(): RoundEntity {
            val game = GameEntity(creator = testUser, maxNumberOfPlayers = 4, seed = ByteArray(256)).let {
                gameRepository.save(it)
            }

            return RoundEntity(
                game = game,
                dealer = PlayerEntity(game = game, user = testUser, seat = 0).let { playerRepository.save(it) },
                number = 1,
                seed = ByteArray(256)
            ).let { roundRepository.save(it) }
        }
    }
}