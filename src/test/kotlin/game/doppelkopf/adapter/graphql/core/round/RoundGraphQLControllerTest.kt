package game.doppelkopf.adapter.graphql.core.round

import game.doppelkopf.BaseGraphQLTest
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested

class RoundGraphQLControllerTest : BaseGraphQLTest() {
    @BeforeEach
    @AfterEach
    fun `unmock all`() {
        clearAllMocks()
        unmockkAll()
    }

    @Nested
    inner class Queries {

    }

    @Nested
    inner class Mutations {

    }
}