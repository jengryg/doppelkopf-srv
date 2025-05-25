package game.doppelkopf.adapter.graphql.common

import java.time.Instant

data class StartedEnded(
    val started: Instant?,
    val ended: Instant?,
)