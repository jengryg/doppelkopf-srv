package game.doppelkopf.common.errors

import game.doppelkopf.errors.ApplicationRuntimeException
import org.springframework.http.HttpStatus

class ForbiddenActionException(
    reason: String,
    cause: Throwable? = null
) : ApplicationRuntimeException(
    HttpStatus.FORBIDDEN,
    cause
) {
    init {
        setTitle("Forbidden action")
        setDetail("You are not allowed to perform this action: $reason")
    }
}

/**
 * Instantiate [ForbiddenActionException] inside a [Result.failure].
 *
 * @param T the type of the wrapped value in [Result]
 * @param reason see [ForbiddenActionException]
 * @param cause see [ForbiddenActionException]
 *
 * @return a [Result] wrapping type [T] in [Result.failure] state with an [ForbiddenActionException]
 */
fun <T> Result.Companion.ofForbiddenAction(
    reason: String,
    cause: Throwable? = null
): Result<T> {
    return failure(ForbiddenActionException(reason, cause))
}