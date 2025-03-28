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