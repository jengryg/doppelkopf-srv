package game.doppelkopf.core.common

import java.time.Instant

/**
 * [IStartedEnded] defines the [started] and [ended] fields of type [Instant].
 * These properties should be used to record the instance of time a model object is considered to be started and ended.
 */
interface IStartedEnded {
    /**
     * [Instant] when this object was started.
     */
    var started: Instant?

    /**
     * [Instant] when this object was ended.
     */
    var ended: Instant?
}