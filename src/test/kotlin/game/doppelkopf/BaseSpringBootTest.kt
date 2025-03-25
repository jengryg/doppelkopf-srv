package game.doppelkopf

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(TestcontainersConfiguration::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag(TestTags.SPRING_INTEGRATION)
abstract class BaseSpringBootTest {
    /**
     * The [SpringBootTest.WebEnvironment.RANDOM_PORT] is injected into this variable for later usage.
     */
    @LocalServerPort
    var serverPort: Int = 0
}