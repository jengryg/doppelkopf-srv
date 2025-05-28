package game.doppelkopf.adapter.graphql.core.turn.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.adapter.graphql.core.hand.dto.PublicHand
import game.doppelkopf.adapter.graphql.core.round.dto.Round
import game.doppelkopf.adapter.graphql.core.trick.dto.Trick
import game.doppelkopf.adapter.persistence.model.turn.TurnEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import java.util.*

data class Turn(
    val id: UUID,
    val number: Int,
    val card: String,
    private val _cu: Lazy<CreatedUpdated>,
    private val _round: Lazy<Round>,
    private val _publicHand: Lazy<PublicHand>,
    private val _trick: Lazy<Trick>,
) {
    val cu: CreatedUpdated by _cu
    val round: Round by _round
    val hand: PublicHand by _publicHand
    val trick: Trick by _trick

    constructor(entity: TurnEntity, currentUser: UserEntity) : this(
        id = entity.id,
        number = entity.number,
        card = entity.card,
        _cu = lazy { CreatedUpdated(entity) },
        _round = lazy { Round(entity.round, currentUser) },
        _publicHand = lazy { PublicHand(entity.hand, currentUser) },
        _trick = lazy { Trick(entity.trick, currentUser) },
    )
}
