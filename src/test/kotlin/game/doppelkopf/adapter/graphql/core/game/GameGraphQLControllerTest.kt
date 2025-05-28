package game.doppelkopf.adapter.graphql.core.game

import game.doppelkopf.BaseGraphQLTest
import game.doppelkopf.adapter.persistence.model.game.GameRepository
import game.doppelkopf.fragName
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.test.tester.GraphQlTester
import java.util.*

class GameGraphQLControllerTest : BaseGraphQLTest() {
    @Autowired
    private lateinit var gameRepository: GameRepository

    @BeforeEach
    @AfterEach
    fun `unmock all`() {
        clearAllMocks()
        unmockkAll()
    }

    @Nested
    inner class Queries {
        @Test
        fun `get list with no games in database return 200 empty list`() {
            gameRepository.deleteAll()

            getGameList()
                .executeAndVerify()
        }

        @Test
        fun `get specific with unknown uuid returns error`() {
            getGame(zeroId).execute()
                .errors()
                .satisfy {
                    assertThat(it).anySatisfy { error -> error.message?.contains("Entity not Found") }
                }
        }
    }

    @Nested
    inner class Mutations {
        @ParameterizedTest
        @ValueSource(ints = [4, 5, 6, 7, 8])
        fun `create with valid player limit returns 201 and dto`(playerLimit: Int) {
            createGame(playerLimit)
                .executeAndVerify()
        }

        @ParameterizedTest
        @ValueSource(ints = [0, 1, 2, 3, -17, 42])
        fun `create with invalid player limit returns error`(playerLimit: Int) {
            createGame(playerLimit)
                .execute().errors().satisfy {
                    assertThat(it).anySatisfy { error -> error.message?.contains("Validation failed for field") }
                }
        }
    }

    private fun getGameList(): GraphQlTester.Request<*> {
        return gqlUserTester
            .documentName("getGameList")
            .fragName("gameProperties")
            .fragName("cu")
            .fragName("se")
    }

    private fun getGame(gameId: UUID): GraphQlTester.Request<*> {
        return gqlUserTester
            .documentName("getGame")
            .fragName("gameProperties")
            .fragName("cu")
            .fragName("se")
            .variable("gameId", gameId)
    }

    private fun createGame(playerLimit: Int): GraphQlTester.Request<*> {
        return gqlUserTester
            .documentName("createGame")
            .fragName("gameProperties")
            .fragName("cu")
            .fragName("se")
            .variable("playerLimit", playerLimit)
    }

    private fun startGame(gameId: UUID): GraphQlTester.Request<*> {
        return gqlUserTester
            .documentName("startGame")
            .fragName("gameProperties")
            .fragName("cu")
            .fragName("se")
            .variable("gameId", gameId)
    }
}