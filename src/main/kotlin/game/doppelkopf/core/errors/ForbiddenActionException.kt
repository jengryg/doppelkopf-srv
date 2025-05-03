package game.doppelkopf.core.errors

import game.doppelkopf.errors.ApplicationRuntimeException
import org.springframework.http.HttpStatus

class ForbiddenActionException(
    action: String,
    reason: String? = null,
    cause: Throwable? = null
) : ApplicationRuntimeException(
    HttpStatus.FORBIDDEN,
    cause
) {
    init {
        setTitle("Forbidden action")
        if (reason != null) {
            setDetail("You are not allowed to perform the action '$action': $reason")
        } else {
            setDetail("You are not allowed to perform the action '$action'.")
        }
    }
}

/**
 * Instantiate [ForbiddenActionException] inside a [Result.failure].
 *
 * @param T the type of the wrapped value in [Result]
 * @param action see [ForbiddenActionException]
 * @param reason see [ForbiddenActionException]
 * @param cause see [ForbiddenActionException]
 *
 * @return a [Result] wrapping type [T] in [Result.failure] state with an [ForbiddenActionException]
 */
fun <T> Result.Companion.ofForbiddenAction(
    action: String,
    reason: String? = null,
    cause: Throwable? = null
): Result<T> {
    return failure(ForbiddenActionException(action, reason, cause))
}