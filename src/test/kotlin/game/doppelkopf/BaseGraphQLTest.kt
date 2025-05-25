package game.doppelkopf

import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import org.junit.jupiter.api.BeforeAll
import org.springframework.graphql.test.tester.HttpGraphQlTester
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

/**
 * Set up the SpringBoot Integration testing for GraphQL based API testing against the Spring Web Environment.
 *
 * Using this base for test implementations automatically creates the [testUser], [testAdmin] and [testPlayers].
 *
 * Provides [gqlTester] without active login, the [gqlUserTester] that has a login as [testUser] and [gqlAdminTester]
 * that has a login as [testAdmin].
 *
 * Additional [HttpGraphQlTester] for specific username and password login credentials can be created using the
 * provided [initializeGraphQlTesterForCredentials] method.
 */
abstract class BaseGraphQLTest : BaseSpringBootTest(), Logging {
    private val log = logger()

    private lateinit var baseUrl: String
    private lateinit var gqlUrl: String

    /**
     * [HttpGraphQlTester] without any login attached to it.
     */
    val gqlTester: HttpGraphQlTester by lazy {
        val client = WebTestClient.bindToServer().baseUrl(gqlUrl).build()
        HttpGraphQlTester.create(client)
    }

    /**
     * [HttpGraphQlTester] using login as [testAdmin].
     * Convenience property lazy populated using [initializeGraphQlTesterForCredentials].
     */
    val gqlAdminTester: HttpGraphQlTester by lazy {
        initializeGraphQlTesterForCredentials(testAdminName, testAdminPassword)
    }

    /**
     * [HttpGraphQlTester] using login as [testUser].
     * Convenience property lazy populated using [initializeGraphQlTesterForCredentials].
     */
    val gqlUserTester: HttpGraphQlTester by lazy {
        initializeGraphQlTesterForCredentials(testUserName, testUserPassword)
    }


    @BeforeAll
    fun `setup containerized graphQL test`() {
        baseUrl = "http://localhost:${serverPort}"
        gqlUrl = "$baseUrl/graphql"
    }

    /**
     * Performs the login with [username] and [password] and initializes a [HttpGraphQlTester] using a [WebTestClient]
     * with the corresponding `JSESSIONID` set as default cookie.
     */
    fun initializeGraphQlTesterForCredentials(username: String, password: String): HttpGraphQlTester {
        val client = WebTestClient.bindToServer().baseUrl(gqlUrl)
            .defaultCookie("JSESSIONID", loginAndExtractSessionId(username, password))
            .build()

        return HttpGraphQlTester.create(client)
    }

    private fun loginAndExtractSessionId(username: String, password: String): String {
        val client = WebClient.create(baseUrl)
        return client.post().uri("/v1/auth/login")
            .body(
                BodyInserters.fromFormData("username", username).with("password", password)
            )
            .exchangeToMono { response ->
                if (response.statusCode().is2xxSuccessful) {
                    Mono.justOrEmpty(response.cookies()["JSESSIONID"]?.firstOrNull()?.value)
                } else {
                    Mono.error(IllegalStateException("Login failed: ${response.statusCode()}"))
                }
            }.block() ?: throw IllegalStateException("Could not obtain JSESSIONID for $username.")

    }
}