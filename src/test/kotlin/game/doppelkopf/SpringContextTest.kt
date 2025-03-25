package game.doppelkopf

import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import kotlin.test.assertTrue

class SpringContextTest : BaseSpringBootTest(), Logging {
    private val log = logger()

    @Autowired
    private lateinit var environment: Environment

    @Test
    fun `ensure that context loads and test profile is active`() {
        assertTrue { environment.matchesProfiles(SpringProfiles.TEST) }

        log.atInfo()
            .setMessage("Context loaded successfully.")
            .addKeyValue("SpringProfilesActive") { environment.activeProfiles.joinToString(", ") }
            .log()
    }

}
