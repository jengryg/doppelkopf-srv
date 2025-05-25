package game.doppelkopf.adapter.graphql.common

import java.time.Instant

data class CreatedUpdated(
    val created: Instant,
    val updated: Instant,
)
