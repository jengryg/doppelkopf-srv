package game.doppelkopf.common.errors

import game.doppelkopf.errors.ApplicationRuntimeException
import org.springframework.http.HttpStatus

class InvalidActionException(
    action: String,
    reason: String? = null,
    cause: Throwable? = null
) : ApplicationRuntimeException(
    HttpStatus.BAD_REQUEST,
    cause
) {
    init {
        setTitle("Invalid action")
        if (reason != null) {
            setDetail("The action '$action' can not be performed: $reason")
        } else {
            setDetail("The action '$action' can not be performed.")
        }
    }
}

/**
 * Instantiate [InvalidActionException] inside a [Result.failure].
 *
 * @param T the type of the wrapped value in [Result]
 * @param action see [InvalidActionException]
 * @param reason see [InvalidActionException]
 * @param cause see [InvalidActionException]
 *
 * @return a [Result] wrapping type [T] in [Result.failure] state with an [InvalidActionException]
 */
fun <T> Result.Companion.ofInvalidAction(
    action: String,
    reason: String? = null,
    cause: Throwable? = null
): Result<T> {
    return failure(InvalidActionException(action, reason, cause))
}