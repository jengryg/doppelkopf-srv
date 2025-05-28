package game.doppelkopf.adapter.graphql.core.hand

import game.doppelkopf.BaseGraphQLTest
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested

class HandGraphQLControllerTest : BaseGraphQLTest() {
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