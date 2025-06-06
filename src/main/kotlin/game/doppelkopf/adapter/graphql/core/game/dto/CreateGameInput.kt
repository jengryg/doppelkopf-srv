package game.doppelkopf.adapter.graphql.core.game.dto

import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.Range

data class CreateGameInput(
    @field:Range(min = 4, max = 8)
    val playerLimit: Int,

    @field:Size(max = 256)
    val seed: ByteArray? = null,
)
