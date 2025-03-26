package game.doppelkopf

import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

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
abstract class BaseSpringBootTest {
    /**
     * Reset mockk to ensure that every test is executed without any object still mocked from previous tests.
     */
    @BeforeAll
    fun `reset mockk`() {
        clearAllMocks()
        unmockkAll()
    }
}