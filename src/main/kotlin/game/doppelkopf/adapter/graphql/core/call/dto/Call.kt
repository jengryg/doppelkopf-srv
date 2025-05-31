package game.doppelkopf.adapter.graphql.core.call.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.adapter.graphql.core.hand.dto.PublicHand
import game.doppelkopf.adapter.persistence.model.call.CallEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.domain.call.enums.CallType
import java.util.*

data class Call(
    val id: UUID,
    val callType: CallType,
    val description: String,
    val cardsPlayedBefore: Int,
    private val _hand : Lazy<PublicHand>,
    private val _cu : Lazy<CreatedUpdated>,
) {
    val cu: CreatedUpdated by _cu
    val hand: PublicHand by _hand

    constructor(entity: CallEntity, currentUser: UserEntity?) : this(
        id = entity.id,
        _cu = lazy { CreatedUpdated(entity) },
        _hand = lazy { PublicHand(entity.hand, currentUser) },
        callType = entity.callType,
        // TODO: we may want to refactor the way the callType and identifiers are handled to simplify this
        description = entity.callType.publicIdentifiers.get(entity.hand.publicTeam) ?: entity.callType.name,
        cardsPlayedBefore = entity.cardsPlayedBefore,
    )
}