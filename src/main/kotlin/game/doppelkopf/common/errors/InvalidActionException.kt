package game.doppelkopf.common.errors

import game.doppelkopf.errors.ApplicationRuntimeException
import org.springframework.http.HttpStatus

class InvalidActionException(
    reason: String,
    cause: Throwable? = null
) : ApplicationRuntimeException(
    HttpStatus.BAD_REQUEST,
    cause
) {
    init {
        setTitle("Invalid action")
        setDetail("This action can not be performed: $reason")
    }
}

/**
 * Instantiate [InvalidActionException] inside a [Result.failure].
 *
 * @param T the type of the wrapped value in [Result]
 * @param reason see [InvalidActionException]
 * @param cause see [InvalidActionException]
 *
 * @return a [Result] wrapping type [T] in [Result.failure] state with an [InvalidActionException]
 */
fun <T> Result.Companion.ofInvalidAction(
    reason: String,
    cause: Throwable? = null
): Result<T> {
    return failure(InvalidActionException(reason, cause))
}