package game.doppelkopf.core.common.errors

import game.doppelkopf.errors.ApplicationRuntimeException
import org.springframework.http.HttpStatus
import java.util.*

/**
 * This exception indicates that something went really wrong in the game.
 * It should be thrown when operations are not possible because expected conditions are not met, data is not available
 * even if it should be available etc.
 */
class GameFailedException(
    reason: String,
    val gameId: UUID,
    cause: Throwable? = null,
) : ApplicationRuntimeException(
    HttpStatus.INTERNAL_SERVER_ERROR,
    cause
) {
    init {
        setTitle("Game Failed")
        setDetail("An error occurred during game processing: $reason")
    }
}

/**
 * Instantiate [GameFailedException] inside a [Result.failure].
 *
 * @param T the type of the wrapped value in [Result]
 * @param reason see [GameFailedException]
 * @param gameId see [GameFailedException]
 * @param cause see [GameFailedException]
 *
 * @return a [Result] wrapping type [T] in [Result.failure] state with an [ForbiddenActionException]
 */
fun <T> Result.Companion.ofGameFailed(
    reason: String,
    gameId: UUID,
    cause: Throwable? = null
): Result<T> {
    return failure(GameFailedException(reason, gameId, cause))
}