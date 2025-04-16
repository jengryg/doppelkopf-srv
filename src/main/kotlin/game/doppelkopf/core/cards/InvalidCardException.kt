package game.doppelkopf.core.cards

import game.doppelkopf.errors.ApplicationRuntimeException
import org.springframework.http.HttpStatus

class InvalidCardException(
    encodedCard: String,
    cause: Throwable? = null
) : ApplicationRuntimeException(
    HttpStatus.BAD_REQUEST,
    cause
) {
    init {
        setTitle("Invalid card encoding.")
        setDetail("Encoding '$encodedCard' does not match any card of the deck.")
    }
}

/**
 * Instantiate [InvalidCardException] inside a [Result.failure].
 *
 * @param T the type of the wrapped value in [Result]
 * @param encodedCard see [InvalidCardException]
 * @param cause see [InvalidCardException]
 *
 * @return a [Result] wrapping type [T] in [Result.failure] state with an [InvalidCardException]
 */
fun <T> Result.Companion.ofInvalidCardException(
    encodedCard: String,
    cause: Throwable? = null
): Result<T> {
    return failure(InvalidCardException(encodedCard, cause))
}