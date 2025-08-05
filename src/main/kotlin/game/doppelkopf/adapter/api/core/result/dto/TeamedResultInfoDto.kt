package game.doppelkopf.adapter.api.core.result.dto

import game.doppelkopf.adapter.persistence.model.result.ResultEntity
import game.doppelkopf.utils.Teamed
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Note: This dto does not use the [Teamed] generic class to ensure type safe dto generation and also allow for simpler
 * parsing due to type erasure of the generic type wrapped by [Teamed].
 */
@Schema(
    description = "Information about the team results of a round in a game of Doppelkopf.",
)
class TeamedResultInfoDto(
    @field:Schema(
        description = "The result information for the RE team."
    )
    val re: ResultInfoDto?,

    @field:Schema(
        description = "The result information for the KO team."
    )
    val ko: ResultInfoDto?
) {
    constructor(teamedEntities: Teamed<ResultEntity?>) : this(
        re = teamedEntities.re?.let { ResultInfoDto(teamedEntities.re) },
        ko = teamedEntities.ko?.let { ResultInfoDto(teamedEntities.ko) },
    )
}