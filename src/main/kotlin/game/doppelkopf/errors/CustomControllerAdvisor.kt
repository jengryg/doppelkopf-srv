package game.doppelkopf.errors

import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * [CustomControllerAdvisor] can be used to handle exceptions we use in this application.
 * It is executed before [HttpControllerAdvisor] that catches all unhandled exceptions.
 *
 * You can define your own handlers in any [RestControllerAdvice] that has [Ordered.HIGHEST_PRECEDENCE] or in this
 * class here.
 */
@Suppress("unused")
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
class CustomControllerAdvisor : Logging {
    private val log = logger()
}