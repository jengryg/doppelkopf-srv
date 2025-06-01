package game.doppelkopf.adapter.graphql.core.round.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.adapter.persistence.model.result.ResultEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.domain.hand.enums.DefiniteTeam
import java.util.*

data class RoundResult(
    val id: UUID,
    val team: DefiniteTeam,
    val trickCount: Int,
    val scoreObtained: Int,
    val scoreTarget: Int,
    private val _cu: Lazy<CreatedUpdated>,
    private val _round: Lazy<Round>,
    private val _points: Lazy<PointsResult>,
) {
    val cu: CreatedUpdated by _cu
    val round: Round by _round
    val points: PointsResult by _points

    constructor(entity: ResultEntity, currentUser: UserEntity) : this(
        id = entity.id,
        team = entity.team,
        trickCount = entity.trickCount,
        scoreObtained = entity.score,
        scoreTarget = entity.target,
        _cu = lazy { CreatedUpdated(entity) },
        _round = lazy { Round(entity.round, currentUser) },
        _points = lazy { PointsResult(entity) }
    )
}