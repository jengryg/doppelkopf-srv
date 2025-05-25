package game.doppelkopf.adapter.graphql.core

import game.doppelkopf.BaseGraphQLTest
import game.doppelkopf.adapter.persistence.model.game.GameRepository
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class GameGraphQlTest : BaseGraphQLTest() {
    @Autowired
    private lateinit var gameRepository: GameRepository

    @BeforeEach
    @AfterEach
    fun `unmock all`() {
        clearAllMocks()
        unmockkAll()
    }

    @Nested
    inner class GetAndList {
        @Test
        fun `get specific with unknown uuid returns error`() {
            val document = """
                {
                    game(id: "$zeroId") {
                        id
                        
                    }
                }
            """.trimIndent()

            gqlUserTester.document(document).execute().errors().satisfy {
                assertThat(it).anySatisfy { error -> error.message?.contains("Entity not Found") }
            }
        }
    }
}