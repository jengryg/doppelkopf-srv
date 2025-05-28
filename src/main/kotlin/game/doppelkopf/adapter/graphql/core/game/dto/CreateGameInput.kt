package game.doppelkopf.adapter.graphql.core.game.dto

import org.hibernate.validator.constraints.Range

data class CreateGameInput(
    @field:Range(min = 4, max = 8)
    val playerLimit: Int
)
