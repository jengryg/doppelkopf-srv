package game.doppelkopf.adapter.graphql.user

import game.doppelkopf.BaseGraphQLTest
import game.doppelkopf.fragName
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserGraphQLControllerTest : BaseGraphQLTest() {
    @BeforeEach
    @AfterEach
    fun `unmock all`() {
        clearAllMocks()
        unmockkAll()
    }

    @Nested
    inner class Queries {
        @Test
        fun `obtain private user information`() {
            gqlAdminTester.documentName("privateUser")
                .fragName("cu")
                .executeAndVerify()

            gqlUserTester.documentName("privateUser")
                .fragName("cu")
                .executeAndVerify()
        }

        @Test
        fun `obtain public user information`() {
            gqlAdminTester.documentName("publicUser")
                .variable("id", testUser.id)
                .executeAndVerify()

            gqlUserTester.documentName("publicUser")
                .variable("id", testAdmin.id)
                .executeAndVerify()
        }
    }

    @Nested
    inner class Mutations {

    }
}