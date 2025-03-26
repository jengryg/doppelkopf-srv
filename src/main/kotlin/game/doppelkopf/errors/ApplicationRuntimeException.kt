package game.doppelkopf.errors

import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException

/**
 * [ApplicationRuntimeException] is used as the base class for all [RuntimeException] in this application.
 * See [ErrorResponseException] for more information on the RFC 9457 conformity.
 *
 * You SHOULD catch other exception where they occur, try to recover if possible, write logs if needed using the
 * [game.doppelkopf.instrumentation.logging.Logging] interface and then throw your own exception class that derives
 * from this class.
 *
 * This class is just extending [ApplicationRuntimeException] to bridge the java implementation to Kotlin for easier usage.
 */
open class ApplicationRuntimeException(
    status: HttpStatusCode,
    body: ProblemDetail,
    cause: Throwable? = null,
    messageDetailCode: String? = null,
    messageDetailArguments: Array<out Any>? = null

) : ErrorResponseException(
    status, body, cause, messageDetailCode, messageDetailArguments
)