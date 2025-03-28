package game.doppelkopf.core.errors

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