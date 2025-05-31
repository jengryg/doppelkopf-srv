package game.doppelkopf.adapter.graphql.user

import game.doppelkopf.BaseGraphQLTest
import game.doppelkopf.adapter.graphql.user.dto.PrivateUserResponse
import game.doppelkopf.adapter.graphql.user.dto.PublicUserResponse
import game.doppelkopf.fragName
import game.doppelkopf.toSingleEntity
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.assertj.core.api.Assertions.assertThat
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
            val adminResponse = gqlAdminTester.documentName("privateUser")
                .fragName("cu")
                .execute()
                .toSingleEntity<PrivateUserResponse>()

            assertThat(adminResponse.id).isEqualTo(testAdmin.id)
            assertThat(adminResponse.name).isEqualTo(testAdmin.username)
            assertThat(adminResponse.cu.created).isEqualTo(testAdmin.created)
            assertThat(adminResponse.cu.updated).isEqualTo(testAdmin.updated)

            val userResponse = gqlUserTester.documentName("privateUser")
                .fragName("cu")
                .execute()
                .toSingleEntity<PrivateUserResponse>()

            assertThat(userResponse.id).isEqualTo(testUser.id)
            assertThat(userResponse.name).isEqualTo(testUser.username)
            assertThat(userResponse.cu.created).isEqualTo(testUser.created)
            assertThat(userResponse.cu.updated).isEqualTo(testUser.updated)
        }

        @Test
        fun `obtain public user information`() {
            val adminResponse = gqlAdminTester.documentName("publicUser")
                .variable("id", testUser.id)
                .execute()
                .toSingleEntity<PublicUserResponse>()

            assertThat(adminResponse.id).isEqualTo(testUser.id)
            assertThat(adminResponse.name).isEqualTo(testUser.username)

            val userResponse = gqlUserTester.documentName("publicUser")
                .variable("id", testAdmin.id)
                .execute()
                .toSingleEntity<PublicUserResponse>()

            assertThat(userResponse.id).isEqualTo(testAdmin.id)
            assertThat(userResponse.name).isEqualTo(testAdmin.username)
        }
    }

    @Nested
    inner class Mutations {

    }
}