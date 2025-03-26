package game.doppelkopf.api

import game.doppelkopf.BaseRestAssuredTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class OpenApiConfigurationTest : BaseRestAssuredTest() {

    @ParameterizedTest
    @ValueSource(strings = [
        "/v3/api-docs",
        "/v3/api-docs.yaml",
        "/swagger-ui.html",
        "/v3/api-docs/swagger-config"
    ])
    fun `open api spec is available in test stage without login`(url: String) {
        Given {
            auth().none()
        } When {
            get(url)
        } Then {
            statusCode(200)
        }
    }
}