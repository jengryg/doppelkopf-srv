package game.doppelkopf

import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import game.doppelkopf.persistence.user.UserEntity
import game.doppelkopf.persistence.user.UserRepository
import game.doppelkopf.security.Authority
import io.restassured.RestAssured
import io.restassured.authentication.FormAuthConfig
import org.bouncycastle.util.encoders.Base64
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import java.security.SecureRandom

/**
 * Set up the SpringBoot Integration testing for RestAssured based API testing against the Spring Web Environment.
 *
 * Using this base for test implementations automatically creates the [testUser], [testAdmin] and [testPlayers].
 *
 * Provides [RestAssured] with basic auth config using the [testUser] by default.
 */
class BaseRestAssuredTest : BaseSpringBootTest(), Logging {
    private val log = logger()

    @Autowired
    protected lateinit var userRepository: UserRepository

    /**
     * Test user username.
     */
    protected val testUserName = "test-user"

    /**
     * Test user password is randomly generated.
     */
    protected val testUserPassword =
        Base64.toBase64String(ByteArray(32).also { SecureRandom.getInstanceStrong().nextBytes(it) })!!

    /**
     * Test admin username.
     */
    protected val testAdminName = "test-admin"


    /**
     * Test admin password is randomly generated
     */
    protected val testAdminPassword =
        Base64.toBase64String(ByteArray(32).also { SecureRandom.getInstanceStrong().nextBytes(it) })!!

    /**
     * Test players usernames.
     */
    protected val testPlayerNames = List(8) { "test-player-$it" }

    /**
     * Test players passwords are randomly generated
     */
    protected val testPlayerPasswords = List(8) {
        Base64.toBase64String(ByteArray(32).also { SecureRandom.getInstanceStrong().nextBytes(it) })!!
    }

    /**
     * Test user that can be used to test the API that requires the "USER" authority.
     */
    protected lateinit var testUser: UserEntity

    /**
     * Test admin that can be used to test the API that requires the "ADMIN" authority.
     */
    protected lateinit var testAdmin: UserEntity

    /**
     * Test players #1 to #8 that can be used to test the game.
     */
    protected lateinit var testPlayers: List<UserEntity>

    /**
     * The [SpringBootTest.WebEnvironment.RANDOM_PORT] is injected into this variable for later usage.
     */
    @LocalServerPort
    var serverPort: Int = 0

    @BeforeAll
    fun `setup test user accounts in database`() {
        testUser = createTestUserEntity(testUserName, testUserPassword, Authority.USER)
        testAdmin = createTestUserEntity(testAdminName, testAdminPassword, Authority.ADMIN)

        testPlayers = List(8) {
            createTestUserEntity(testPlayerNames[it], testPlayerPasswords[it], Authority.USER)
        }
    }

    /**
     * Creates user entities for the test runs and save them to the database.
     *
     * @param name the name for the test user
     * @param password the password for the test user
     * @param authority the [Authority] that the user should be assigned
     * @return a [UserEntity] with the given [name], [password] and [authority]
     */
    private fun createTestUserEntity(
        name: String,
        password: String,
        authority: Authority
    ): UserEntity {
        return userRepository.save(
            UserEntity(
                username = name,
                password = PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(password),
                enabled = true,
                locked = false,
                authority = authority
            )
        ).also {
            log.atDebug()
                .setMessage("Created test user account in database.")
                .addKeyValue("user") { it }
                .log()
        }
    }

    @AfterAll
    fun `delete test user accounts from database`() {
        userRepository.deleteAll()
    }

    /**
     * The [FormAuthConfig] for the login api at `/v1/auth/login`.
     */
    protected val formAuthConfig = FormAuthConfig("/v1/auth/login", "username", "password").withLoggingEnabled()

    @BeforeAll
    fun `setup containerized rest assured test`() {
        RestAssured.port = serverPort

        /**
         * Set the default authentication method for RestAssured based tests to the [testUser].
         */
        RestAssured.authentication =
            RestAssured.form(testUserName, testUserPassword, formAuthConfig)

        log.atDebug()
            .setMessage("RestAssured configured.")
            .addKeyValue("port") { serverPort }
            .addKeyValue("authUser") { testUser }
            .log()
    }
}