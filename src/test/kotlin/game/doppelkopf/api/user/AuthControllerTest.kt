package game.doppelkopf.api.user

import game.doppelkopf.BaseRestAssuredTest
import game.doppelkopf.api.user.dto.SimpleUserResponseDto
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AuthControllerTest : BaseRestAssuredTest() {
    @Test
    fun `http GET on login endpoint without having an active login`() {
        Given {
            auth().none()
        } When {
            get("/v1/auth/login")
        } Then {
            statusCode(204)
        } Extract {
            response().body.asByteArray().isEmpty()
        }
    }

    @Test
    fun `http GET on login endpoint with rest assured supported testUser login`() {
        val response = Given {
            auth().form(testUserName, testUserPassword, formAuthConfig)
        } When {
            get("/v1/auth/login")
        } Then {
            statusCode(200)
        } Extract {
            response().`as`(SimpleUserResponseDto::class.java)
        }

        assertEquals(testUser.id, response.id)
        assertEquals(testUser.username, response.name)
    }

    @Test
    fun `http GET on login endpoint with rest assured supported testAdmin login`() {
        val response = Given {
            auth().form(testAdminName, testAdminPassword, formAuthConfig)
        } When {
            get("/v1/auth/login")
        } Then {
            statusCode(200)
        } Extract {
            response().`as`(SimpleUserResponseDto::class.java)
        }

        assertEquals(testAdmin.id, response.id)
        assertEquals(testAdmin.username, response.name)
    }
}