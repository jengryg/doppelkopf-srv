package game.doppelkopf.core.common.errors

import game.doppelkopf.errors.ApplicationRuntimeException
import org.springframework.http.HttpStatus

/**
 * This exception indicates that something went really wrong in the game.
 * It should be thrown when operations are not possible because expected conditions are not met, data is not available
 * even if it should be available etc.
 */
class GameFailedException(
    reason: String,
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