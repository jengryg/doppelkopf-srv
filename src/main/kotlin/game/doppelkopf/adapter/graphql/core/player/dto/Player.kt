package game.doppelkopf.adapter.graphql.core.player.dto

import game.doppelkopf.adapter.graphql.common.CreatedUpdated
import game.doppelkopf.adapter.graphql.core.game.dto.Game
import game.doppelkopf.adapter.graphql.user.dto.PublicUser
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import java.util.*

data class Player(
    val id: UUID,
    val seat: Int,
    val dealer: Boolean,
    private val _cu: Lazy<CreatedUpdated>,
    private val _game: Lazy<Game>,
    private val _user: Lazy<PublicUser>,
) {
    val cu: CreatedUpdated by _cu
    val game: Game by _game
    val user: PublicUser by _user

    constructor(entity: PlayerEntity, currentUser: UserEntity?) : this(
        id = entity.id,
        seat = entity.seat,
        dealer = entity.dealer,
        _cu = lazy { CreatedUpdated(entity) },
        _game = lazy { Game(entity.game, currentUser) },
        _user = lazy { PublicUser(entity.user) },
    )
}
