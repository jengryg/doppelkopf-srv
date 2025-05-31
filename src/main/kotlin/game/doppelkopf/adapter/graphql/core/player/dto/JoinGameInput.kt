package game.doppelkopf.adapter.graphql.core.player.dto

import org.hibernate.validator.constraints.Range
import java.util.*

data class JoinGameInput(
    val gameId: UUID,

    @field:Range(min = 1, max = 7) // seat number 0 is automatically occupied by the game creator
    val seat: Int
)
