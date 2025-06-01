package game.doppelkopf

import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import org.junit.jupiter.api.BeforeAll
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.graphql.support.ResourceDocumentSource
import org.springframework.graphql.test.tester.GraphQlTester
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
 * provided [initializeWebTestClient] method.
 */
abstract class BaseGraphQLTest : BaseSpringBootTest(), Logging {
    private val log = logger()

    private lateinit var baseUrl: String
    private lateinit var gqlUrl: String

    /**
     * [HttpGraphQlTester] without any login attached to it.
     */
    val gqlTester: HttpGraphQlTester by lazy {
        val client = initializeWebTestClient()
        initializeHttpGraphQLTester(client)
    }

    /**
     * [HttpGraphQlTester] using login as [testAdmin].
     * Convenience property lazy populated using [initializeWebTestClient].
     */
    val gqlAdminTester: HttpGraphQlTester by lazy {
        val client = initializeWebTestClient(testAdminName, testAdminPassword)
        initializeHttpGraphQLTester(client)
    }

    /**
     * [HttpGraphQlTester] using login as [testUser].
     * Convenience property lazy populated using [initializeWebTestClient].
     */
    val gqlUserTester: HttpGraphQlTester by lazy {
        val client = initializeWebTestClient(testUserName, testUserPassword)
        initializeHttpGraphQLTester(client)
    }


    @BeforeAll
    fun `setup containerized graphQL test`() {
        baseUrl = "http://localhost:${serverPort}"
        gqlUrl = "$baseUrl/graphql"
    }

    /**
     * Custom document source for the [HttpGraphQlTester] that allows arbitrary subdirectory structures in the default
     * `graphql-test` classpath directory.
     * This implementation only supports the default file extensions that are specified by
     * [ResourceDocumentSource.FILE_EXTENSIONS].
     */
    val customizedDocumentSource = PathMatchingResourcePatternResolver().let { resolver ->
        // Everything in the graphql-test directory on the classpath can be used as source if it ends with one of the
        // default extensions.
        ResourceDocumentSource.FILE_EXTENSIONS.flatMap { extension ->
            resolver.getResources("classpath*:graphql-test/**/?*$extension").toList()
        }.also {
            log.atDebug()
                .setMessage { "Configured classpath resources for the GraphQLlTester." }
                .addKeyValue("locations") {
                    it.joinToString(", ")
                }
                .log()
        }
    }.let { resources ->
        // Construct the ResourceDocumentSource with our customized resource locations and configure the default
        // file extensions.
        ResourceDocumentSource(resources, ResourceDocumentSource.FILE_EXTENSIONS)
    }

    /**
     * Initializes a [HttpGraphQlTester] that uses the given [clientBuilder] and operates on the
     * [customizedDocumentSource].
     */
    fun initializeHttpGraphQLTester(clientBuilder: WebTestClient.Builder): HttpGraphQlTester {
        return HttpGraphQlTester.builder(clientBuilder)
            .documentSource(customizedDocumentSource)
            .build()
    }

    /**
     * Initializes a [WebTestClient.Builder] without `JSESSIONID` cookie set to be used by [HttpGraphQlTester].
     */
    fun initializeWebTestClient(): WebTestClient.Builder {
        return WebTestClient.bindToServer().baseUrl(gqlUrl)
            .codecs {
                it.defaultCodecs()
                    .maxInMemorySize(16_777_216) // 16 * 1024 * 1024 bytes
            }
    }

    /**
     * Performs the login with [username] and [password] and initializes a [WebTestClient.Builder] with the corresponding
     * `JSESSIONID` set as default cookie. Use the returned [WebTestClient] for the [HttpGraphQlTester] to perform
     * authorized requests against the graphql endpoint.
     */
    fun initializeWebTestClient(username: String, password: String): WebTestClient.Builder {
        return WebTestClient.bindToServer().baseUrl(gqlUrl)
            .defaultCookie("JSESSIONID", loginAndExtractSessionId(username, password))
            .codecs {
                it.defaultCodecs()
                    .maxInMemorySize(16_777_216) // 16 * 1024 * 1024 bytes
            }
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

/**
 * Shortcut method: `fragmentNameDot("someName")` is equivalent to `fragmentName("someName.fragment")`.
 */
fun <T : GraphQlTester.Request<T>> GraphQlTester.Request<T>.fragName(fragmentName: String): GraphQlTester.Request<T> {
    return this.fragmentName("${fragmentName}.fragment")
}

/**
 * Get the content of the `data` attribute as [Map] with arbitrary types.
 */
fun GraphQlTester.Response.data(): Map<*, *> {
    return this.path("").entity(Map::class.java).get()
}

/**
 * Cast the value of th given [path] to type [T] wrapped by [GraphQlTester.Entity] and returns it.
 */
inline fun <reified T> GraphQlTester.Response.toEntity(path: String): T {
    return this.path(path).entity(T::class.java).get()
}

/**
 * Collects and casts all properties of the `data` attribute into an array with elements of type [T] wrapped as
 * [GraphQlTester.EntityList], ensures that there is only one element, i.e. `data` had only one single property
 * and returns the value of that property as type [T].
 */
inline fun <reified T> GraphQlTester.Response.toSingleEntity(): T {
    return this.path("$.data[*]").entityList(T::class.java).hasSize(1).get().single()
}

/**
 * Cast the value of the given [path] to type [T] wrapping [GraphQlTester.Entity] and validate the result with the
 * [block] executing in inside [GraphQlTester.Entity.satisfies].
 */
inline fun <reified T> GraphQlTester.Response.entitySatisfies(path: String, crossinline block: (T) -> Unit) {
    this.path(path).entity(T::class.java).satisfies { block(it) }
}