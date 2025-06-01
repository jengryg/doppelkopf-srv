package game.doppelkopf.playtest

import game.doppelkopf.BaseRestAssuredTest
import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import org.junit.jupiter.api.Test
import kotlin.io.encoding.ExperimentalEncodingApi

class BruteforceApiPlayTest : BaseRestAssuredTest(), Logging {
    private val log = logger()

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun `playtest a game over api with brute force strategy`() {

    }
}