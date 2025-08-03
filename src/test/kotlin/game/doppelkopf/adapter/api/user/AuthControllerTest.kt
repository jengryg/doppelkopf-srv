package game.doppelkopf.adapter.api.user

import game.doppelkopf.BaseRestAssuredTest
import game.doppelkopf.adapter.api.user.dto.PublicUserInfoDto
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AuthControllerTest : BaseRestAssuredTest() {
    @Nested
    inner class AuthStatus {
        @Test
        fun `http GET on login endpoint without having an active login is 401`() {
            Given {
                auth().none()
            } When {
                get("/v1/auth/status")
            } Then {
                statusCode(401)
            }
        }

        @Test
        fun `http GET on login endpoint with rest assured supported testUser login returns user`() {
            val response = Given {
                auth().form(testUserName, testUserPassword, formAuthConfig)
            } When {
                get("/v1/auth/status")
            } Then {
                statusCode(200)
            } Extract {
                response().`as`(PublicUserInfoDto::class.java)
            }

            assertEquals(testUser.id, response.id)
            assertEquals(testUser.username, response.name)
        }

        @Test
        fun `http GET on login endpoint with rest assured supported testAdmin login returns admin`() {
            val response = Given {
                auth().form(testAdminName, testAdminPassword, formAuthConfig)
            } When {
                get("/v1/auth/status")
            } Then {
                statusCode(200)
            } Extract {
                response().`as`(PublicUserInfoDto::class.java)
            }

            assertEquals(testAdmin.id, response.id)
            assertEquals(testAdmin.username, response.name)
        }
    }
}