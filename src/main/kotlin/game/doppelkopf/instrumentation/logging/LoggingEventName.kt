@file:Suppress("unused")

package game.doppelkopf.instrumentation.logging

import game.doppelkopf.instrumentation.logging.LoggingEventName.Companion.LOGGING_KEY


/**
 * Implementors of this [LoggingEventName] interface can be used to tag log messages with a [LOGGING_KEY] key and the
 * value returned by [name].
 *
 * Adding [LoggingEventName] to log messages provides a structured way to categorize logging messages.
 */
interface LoggingEventName {
    val name: String

    companion object {
        const val LOGGING_KEY = "LEN"
    }
}