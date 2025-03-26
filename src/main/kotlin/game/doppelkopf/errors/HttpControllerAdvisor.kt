package game.doppelkopf.errors

import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler


/**
 * Customize the handlers for [ResponseEntityExceptionHandler] methods.
 *
 * [Ordered.HIGHEST_PRECEDENCE] + 2 exception handler containing the [handleGeneralException] to catch all unhandled
 * exception explicitly. You can define your own handlers in any [RestControllerAdvice] that has
 * [Ordered.HIGHEST_PRECEDENCE] or add another fallback in the [CustomControllerAdvisor] that has
 * [Ordered.HIGHEST_PRECEDENCE] + 1.
 *
 * Note: [Ordered] uses precedence scores where the highest one is the smallest singled integer and the lowest is the
 * largest signed integer, thus increasing the value reduces the precedence. [Int.MIN_VALUE] is the first handler.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
class HttpControllerAdvisor : ResponseEntityExceptionHandler(), Logging {
    private val log = logger()

    /**
     * Handle general exception
     *
     * Catches all [Exception] that are not handled elsewhere and builds the [ProblemDetail] without information about
     * the [Exception] to avoid information leakage.
     *
     * @param ex the exception
     * @return a [ProblemDetail] instance for [HttpStatus.INTERNAL_SERVER_ERROR] without information from [ex]
     */
    @ExceptionHandler(
        value = [Exception::class]
    )
    fun handleGeneralException(
        ex: Exception
    ): ProblemDetail {
        log.atError()
            .setMessage("Exception caught by general exception handler.")
            .setCause(ex)
            .log()
        // This is logged at ERROR level since we do not expect to have unhandled exceptions in this application.

        return ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase
        ).also {
            it.title = "Application Error"
        }
    }
}