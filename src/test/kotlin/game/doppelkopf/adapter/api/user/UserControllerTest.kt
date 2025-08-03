package game.doppelkopf.adapter.api.user

import game.doppelkopf.BaseRestAssuredTest
import game.doppelkopf.adapter.api.user.dto.PublicUserInfoDto
import game.doppelkopf.adapter.api.user.dto.UserRegisterDto
import game.doppelkopf.errors.ProblemDetailResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserControllerTest : BaseRestAssuredTest() {
    @Nested
    inner class GetSpecific {
        @Test
        fun `get specific with unknown uuid returns 404`() {
            val response = getResource<ProblemDetailResponse>(
                path = "/v1/users/$zeroId",
                expectedStatus = 404
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/users/$zeroId")
            assertThat(response.title).isEqualTo("Entity not found")
            assertThat(response.detail).isEqualTo("The entity of type UserEntity with id $zeroId was not found.")
        }

        @Test
        fun `get specific by its id returns 200 and dto`() {
            val response = getResource<PublicUserInfoDto>(
                path = "/v1/users/${testAdmin.id}",
                expectedStatus = 200
            )

            assertThat(response.id).isEqualTo(testAdmin.id)
            assertThat(response.name).isEqualTo(testAdmin.username)
        }
    }

    @Nested
    inner class AuthRegister {
        @Test
        fun `register with password to short returns 400`() {
            val (response, location) = execRegisterUser<ProblemDetailResponse>(
                username = "UserFailure",
                password = "short",
                passwordConfirm = "short",
                expectedStatus = 400
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/users")
            assertThat(response.title).isEqualTo("Bad Request")
            assertThat(response.detail).isEqualTo("Invalid request content.")

            assertThat(location).isNull()
        }

        @Test
        fun `register with password to long returns 400`() {
            val (response, location) = execRegisterUser<ProblemDetailResponse>(
                username = "UserFailure",
                password = "long".repeat(256),
                passwordConfirm = "long".repeat(256),
                expectedStatus = 400
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/users")
            assertThat(response.title).isEqualTo("Bad Request")
            assertThat(response.detail).isEqualTo("Invalid request content.")

            assertThat(location).isNull()
        }

        @Test
        fun `register with password not matching returns 400`() {
            val (response, location) = execRegisterUser<ProblemDetailResponse>(
                username = "UserFailure",
                password = "Password17",
                passwordConfirm = "Password42",
                expectedStatus = 400
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/users")
            assertThat(response.title).isEqualTo("Invalid action")
            assertThat(response.detail).isEqualTo("This action can not be performed: Passwords do not match.")

            assertThat(location).isNull()
        }

        @Test
        fun `register with already existing username returns 409`() {
            val (response, location) = execRegisterUser<ProblemDetailResponse>(
                username = testAdmin.username,
                password = "Password42",
                passwordConfirm = "Password42",
                expectedStatus = 409
            )

            assertThat(response.instance.toString()).isEqualTo("/v1/users")
            assertThat(response.title).isEqualTo("Username already exists.")
            assertThat(response.detail).isEqualTo("There is already an account with the username test-admin.")

            assertThat(location).isNull()
        }

        @Test
        fun `register with valid data creates account`() {
            val (response, location) = execRegisterUser<PublicUserInfoDto>(
                username = "UserSuccess",
                password = "Password42",
                passwordConfirm = "Password42",
                expectedStatus = 201
            )

            response.also {
                assertThat(response.name).isEqualTo("UserSuccess")
            }

            assertThat(location).isNotNull()

            getResource<PublicUserInfoDto>(location!!, 200).also {
                assertThat(it.id).isEqualTo(response.id)
                assertThat(it.name).isEqualTo("UserSuccess")
            }
        }
    }

    private final inline fun <reified T> execRegisterUser(
        username: String,
        password: String,
        passwordConfirm: String,
        expectedStatus: Int
    ): Pair<T, String?> {
        return createResource<UserRegisterDto, T>(
            path = "/v1/users",
            body = UserRegisterDto(
                username = username,
                password = password,
                passwordConfirm = passwordConfirm
            ),
            expectedStatus = expectedStatus
        )
    }
}