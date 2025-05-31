package game.doppelkopf.adapter.graphql.core.hand.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.adapter.graphql.core.call.dto.Call
import game.doppelkopf.adapter.graphql.core.player.dto.Player
import game.doppelkopf.adapter.graphql.core.round.dto.Round
import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.domain.hand.enums.BiddingPublic
import game.doppelkopf.domain.hand.enums.DeclarationPublic
import game.doppelkopf.domain.hand.enums.Team
import java.util.*

data class PublicHand(
    val id: UUID,
    val declared: DeclarationPublic,
    val bid: BiddingPublic,
    val team: Team?,
    private val _cu: Lazy<CreatedUpdated>,
    private val _player: Lazy<Player>,
    private val _round: Lazy<Round>,
    private val _calls: Lazy<List<Call>>
) {
    val cu: CreatedUpdated by _cu
    val player: Player by _player
    val round: Round by _round
    val calls: List<Call> by _calls

    constructor(entity: HandEntity, currentUser: UserEntity?) : this(
        id = entity.id,
        declared = entity.declared.declarationPublic,
        bid = entity.bidding.biddingPublic,
        team = entity.publicTeam,
        _cu = lazy { CreatedUpdated(entity) },
        _player = lazy { Player(entity.player, currentUser) },
        _round = lazy { Round(entity.round, currentUser) },
        _calls = lazy { entity.calls.map { Call(it, currentUser) } },
    )
}