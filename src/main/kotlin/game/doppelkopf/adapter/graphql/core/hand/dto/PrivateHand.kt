package game.doppelkopf.adapter.graphql.core.hand.dto

import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.domain.hand.enums.Team
import java.util.*

data class PrivateHand(
    val id: UUID,
    val team: Team,
    val cardsPlayed: List<String>,
    val cardsRemaining: List<String>,
    private val _public: Lazy<PublicHand>,
) {
    val public: PublicHand by _public

    constructor(entity: HandEntity, currentUser: UserEntity) : this(
        id = entity.id,
        _public = lazy { PublicHand(entity, currentUser) },
        team = entity.playerTeam,
        cardsPlayed = entity.cardsPlayed,
        cardsRemaining = entity.cardsRemaining
    )
}
