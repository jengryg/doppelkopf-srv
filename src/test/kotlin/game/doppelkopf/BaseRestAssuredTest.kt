package game.doppelkopf

import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import io.restassured.RestAssured
import io.restassured.authentication.FormAuthConfig
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.BeforeAll
import org.springframework.http.HttpHeaders
import java.util.*

/**
 * Set up the SpringBoot Integration testing for RestAssured based API testing against the Spring Web Environment.
 *
 * Using this base for test implementations automatically creates the [testUser], [testAdmin] and [testPlayers].
 *
 * Provides [RestAssured] with basic auth config using the [testUser] by default.
 */
class BaseRestAssuredTest : BaseSpringBootTest(), Logging {
    private val log = logger()

    /**
     * The [FormAuthConfig] for the login api at `/v1/auth/login`.
     */
    protected val formAuthConfig = FormAuthConfig("/v1/auth/login", "username", "password")
        .withLoggingEnabled()!!

    @BeforeAll
    fun `setup containerized rest assured test`() {
        RestAssured.port = serverPort

        /**
         * Set the default authentication method for RestAssured based tests to the [testUser].
         */
        RestAssured.authentication =
            RestAssured.form(testUserName, testUserPassword, formAuthConfig)

        if (log.isDebugEnabled) {
            // print request and responses contents to console when debug log level is enabled
            RestAssured.filters(RequestLoggingFilter(), ResponseLoggingFilter())
        }

        log.atDebug()
            .setMessage("RestAssured configured.")
            .addKeyValue("port") { serverPort }
            .addKeyValue("authUser") { testUser }
            .log()
    }

    /**
     * Executes the post request at the given [path] that allows to create a resource.
     * This method does not use the HTTP Body to send data to the API, see other overloads of [createResource] for that.
     *
     * The response HTTP Status Code is expected to be [expectedStatus] and then the response body is cast to [T].
     * Additionally, the HTTP Response Headers are checked for a [HttpHeaders.LOCATION] entry and extracted if present.
     *
     * @param T the type to use for the casting of the HTTP Response body
     *
     * @param path for HTTP POST
     * @param expectedStatus the HTTP Status Code of the response we expect
     *
     * @return the Pair containing the response cast to [T] and the location reference for the resource as [String] if
     * the location header was present in the response
     */
    protected final inline fun <reified T> createResource(
        path: String,
        expectedStatus: Int
    ): Pair<T, String?> {
        return Given {
            contentType(ContentType.JSON)
        } When {
            post(path)
        } Then {
            statusCode(expectedStatus)
        } Extract {
            Pair(
                first = response().`as`(T::class.java),
                second = response().headers.let {
                    if (it.hasHeaderWithName(HttpHeaders.LOCATION)) it.get(HttpHeaders.LOCATION).value else null
                }
            )
        }
    }

    /**
     * Executes the POST request at the given [path] that allows to create a resource.
     * The HTTP Request body contains the [ContentType.JSON] of the given [body].
     *
     * The response HTTP Status Code is expected to be [expectedStatus] and then the response body is cast to [T].
     * Additionally, the HTTP Response Headers are checked for a [HttpHeaders.LOCATION] entry and extracted if present.
     *
     * @param S the type of the HTTP Request body
     * @param T the type to use for the casting of the HTTP Response body
     *
     * @param path for HTTP POST
     * @param expectedStatus the HTTP Status Code of the response we expect
     *
     * @return the Pair containing the response cast to [T] and the location reference for the resource as [String] if
     * the location header was present in the response
     */
    protected final inline fun <reified S, reified T> createResource(
        path: String,
        body: S,
        expectedStatus: Int,
    ): Pair<T, String?> {
        return Given {
            contentType(ContentType.JSON)
            body(body)
        } When {
            post(path)
        } Then {
            statusCode(expectedStatus)
        } Extract {
            Pair(
                first = response().`as`(T::class.java),
                second = response().headers.let {
                    if (it.hasHeaderWithName(HttpHeaders.LOCATION)) it.get(HttpHeaders.LOCATION).value else null
                }
            )
        }
    }

    /**
     * Executes the PATCH request to the given [path] that allows to modify a resource.
     * The HTTP Request body contains the [ContentType.JSON] of the given [body].
     *
     * The response HTTP Status Code is expected to be [expectedStatus] and then the response body is cast to [T].
     *
     * @param S the type of the HTTP Request body
     * @param T the type to use for the casting of the HTTP Response body
     *
     * @param path for HTTP PATCH
     * @param expectedStatus the HTTP Status Code of the response we expect
     *
     * @return the response body cast to [T]
     */
    protected final inline fun <reified S, reified T> patchResource(
        path: String,
        body: S,
        expectedStatus: Int,
    ): T {
        return Given {
            contentType(ContentType.JSON)
            body(body)
        } When {
            patch(path)
        } Then {
            statusCode(expectedStatus)
        } Extract {
            response().`as`(T::class.java)
        }
    }

    /**
     * Executes the GET request to the given [path] that allows to retrieve a single resource.
     *
     * The response HTTP Status Code is expected to be [expectedStatus] and then the request body is cast to [T].
     *
     * @param T the type to use for the casting of the HTTP Response body
     *
     * @param path for HTTP GET
     * @param expectedStatus the HTTP Status Code of the response we expect
     *
     * @return the response body cast to [T]
     */
    protected final inline fun <reified T> getResource(
        path: String,
        expectedStatus: Int
    ): T {
        return Given {
            this
        } When {
            get(path)
        } Then {
            statusCode(expectedStatus)
        } Extract {
            response().`as`(T::class.java)
        }
    }

    /**
     * Executes the GET request to the given [path] that allows to retrieve a list of resources.
     *
     * The response HTTP Status Code is expected to be [expectedStatus] and then the request body is cast to a [List] of
     * elements of type [T].
     *
     * @param T the type to use for the casting of the elements of the HTTP Response body
     *
     * @param path for HTTP GET
     * @param expectedStatus the HTTP Status Code of the response we expect
     *
     * @return the response body cast to a [List] of [T]
     *
     * @see [getResource]
     */
    protected final inline fun <reified T> getResourceList(
        path: String,
        expectedStatus: Int
    ): List<T> {
        return Given {
            this
        } When {
            get(path)
        } Then {
            statusCode(expectedStatus)
        } Extract {
            response().jsonPath().getList("$", T::class.java)
        }
    }

    companion object {
        val zeroId = UUID.fromString("00000000-0000-0000-0000-000000000000")!!
    }
}