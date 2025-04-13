package game.doppelkopf

import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import game.doppelkopf.persistence.BaseEntityRepository
import game.doppelkopf.persistence.model.user.UserEntity
import game.doppelkopf.persistence.model.user.UserRepository
import game.doppelkopf.security.Authority
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.bouncycastle.util.encoders.Base64
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.metrics.data.DefaultRepositoryTagsProvider
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import java.security.SecureRandom

/**
 * General base class for [SpringBootTest] of this application.
 *
 * Provides [SpringBootTest.WebEnvironment.RANDOM_PORT] with [TestcontainersConfiguration] for Spring Integration tests.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(TestcontainersConfiguration::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag(TestTags.SPRING_INTEGRATION)
abstract class BaseSpringBootTest : Logging {
    @Autowired
    private lateinit var repositoryTagsProvider: DefaultRepositoryTagsProvider
    private val log = logger()

    @Autowired
    protected lateinit var userRepository: UserRepository

    /**
     * Inject all [BaseEntityRepository] of the current spring context.
     * We call [JpaRepository.deleteAll] on all entries in this list [AfterAll] tests are done to ensure an empty
     * test-container based database.
     */
    @Autowired
    protected lateinit var repositories: List<BaseEntityRepository<*>>

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
    fun `execute test setups and clearing to provide suitable test environment`() {
        `setup test user accounts in database`()
        `reset mockk`()
    }

    @AfterAll
    fun `delete all entities in all repositories to provide a clean testcontainers environment`() {
        // Delete all entities in all repositories.
        repositories.forEach { repo ->
            kotlin.runCatching { repo.deleteAll() }.onFailure {
                log.atError()
                    .setMessage("Failed to delete entities in repository after test completed.")
                    .setCause(it)
                    .log()
            }
        }
    }

    fun `setup test user accounts in database`() {
        testUser = createTestUserEntity(testUserName, testUserPassword, Authority.USER)
        testAdmin = createTestUserEntity(testAdminName, testAdminPassword, Authority.ADMIN)

        testPlayers = List(8) {
            createTestUserEntity(testPlayerNames[it], testPlayerPasswords[it], Authority.USER)
        }
    }

    /**
     * Reset mockk to ensure that every test is executed without any object still mocked from previous tests.
     */
    fun `reset mockk`() {
        clearAllMocks()
        unmockkAll()
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
}