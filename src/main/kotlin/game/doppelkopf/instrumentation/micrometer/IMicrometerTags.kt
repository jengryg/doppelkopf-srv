package game.doppelkopf.instrumentation.micrometer

import io.micrometer.core.instrument.Tags

/**
 * Implementations of this interface can be used together with the [MetricUtility] implementation for simple tagging
 * of observation data.
 */
interface IMicrometerTags {
    /**
     * @return the micrometer [Tags] for the implementation
     */
    fun tags(): Tags
}