package game.doppelkopf.adapter.graphql.core.player.dto

import org.hibernate.validator.constraints.Range
import java.util.*

data class JoinGameInput(
    val gameId: UUID,

    @field:Range(min = 0, max = 7)
    val seat: Int
)
