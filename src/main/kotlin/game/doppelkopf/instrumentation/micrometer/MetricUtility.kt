@file:Suppress("unused")

package game.doppelkopf.instrumentation.micrometer

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration
import kotlin.time.toJavaDuration

/**
 * Provides utility methods for [MeterRegistry] to increment counter metrics and record timer metrics.
 */
@Service
class MetricUtility(
    private val meterRegistry: MeterRegistry,
) {
    /**
     * Increment the counter with [name] and given [tags] by [increment].
     *
     * See [Micrometer / Concepts / Counters](https://docs.micrometer.io/micrometer/reference/concepts/counters.html)
     * for more information.
     *
     * @param name the name of the counter metric
     * @param tags the [Tags] of the counter metric
     * @param increment the [Double] amount to add to the counter metric
     */
    fun incrementCounter(
        name: String,
        tags: Tags? = null,
        increment: Double = 1.0
    ) {
        meterRegistry.counter(
            name,
            tags ?: Tags.empty(),
        ).increment(increment)
    }

    /**
     * Record the timer with [name] and given [tags] for [duration].
     *
     * See [Micrometer / Concepts / Timers](https://docs.micrometer.io/micrometer/reference/concepts/timers.html)
     * for more information.
     *
     * @param name te name of the timer metric
     * @param tags the [Tags] of the timer metric
     * @param duration the [Duration] to record
     */
    fun recordTimer(
        name: String,
        tags: Tags? = null,
        duration: Duration
    ) {
        meterRegistry.timer(
            name,
            tags ?: Tags.empty(),
        ).record(
            // duration is a Kotlin duration here
            duration.toJavaDuration(),
        )
    }

    /**
     * Record the timer with [name] and given [tags] for a duration of [amount] units of [unit].
     *
     * @param name the name of the timer metric
     * @param tags the [Tags] of the timer metric
     * @param amount the number of units for the duration to record
     * @param unit the [TimeUnit] in which [amount] is specified
     */
    fun recordTimer(
        name: String,
        tags: Tags? = null,
        amount: Long,
        unit: TimeUnit
    ) {
        meterRegistry.timer(
            name,
            tags ?: Tags.empty(),
        ).record(amount, unit)
    }

    /**
     * Initializes the gauge with the given [name] and optional [tags] and sets the initial value to [initValue].
     * The [AtomicLong] returned by this method must be used to set the current value of the gauge via [AtomicLong.set].
     *
     * See [Micrometer / Concepts / Gauges](https://docs.micrometer.io/micrometer/reference/concepts/gauges.html)
     * for more information.
     *
     * Note: This method always creates only one gauge with the given [name] and [tags].
     * Even if [tags] contains multiple elements, this method returns a single gauge that has all the tags assigned to
     * it. If you want to define multiple gauges with the same name but different tags, each of the gauges must be
     * initialized with this method separately.
     *
     * @param name the name for the gauge
     * @param tags the [Tags] for the gauge
     * @param initValue the [Long] to use as starting value for the gauge
     * @return the [AtomicLong] that must be used to set the current value of the gauge
     */
    fun initGauge(
        name: String,
        tags: Tags? = null,
        initValue: Long = 0L
    ): AtomicLong {
        return meterRegistry.gauge(
            name,
            tags ?: emptyList(),
            AtomicLong(initValue)
        ) ?: throw IllegalStateException("Could not initialize new Micrometer gauge $name with tags: $tags!")
    }
}