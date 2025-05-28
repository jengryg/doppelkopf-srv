package game.doppelkopf.adapter.graphql.common

import game.doppelkopf.common.model.IStartedEnded
import java.time.Instant

data class StartedEnded(
    val started: Instant?,
    val ended: Instant?,
) {
    constructor(entity: IStartedEnded) : this(
        started = entity.started,
        ended = entity.ended,
    )
}