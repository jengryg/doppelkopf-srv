@file:Suppress("unused")

package game.doppelkopf.instrumentation.logging

import org.slf4j.spi.LoggingEventBuilder

/**
 * Add the [LoggingEventName.LOGGING_KEY] key value pair using the [LoggingEventName.name] as value.
 *
 * @param event the [LoggingEventName] defining the name for the [ch.qos.logback.classic.spi.LoggingEvent] constructed
 * by this builder.
 *
 * @return a [LoggingEventBuilder], usually this
 */
fun LoggingEventBuilder.addLoggingEventName(event: LoggingEventName): LoggingEventBuilder {
    return addKeyValue(LoggingEventName.LOGGING_KEY) { event.name }
}