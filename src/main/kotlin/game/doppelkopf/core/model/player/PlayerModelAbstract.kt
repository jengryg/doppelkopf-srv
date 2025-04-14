package game.doppelkopf.core.model.player

import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.core.model.game.GameModel
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.persistence.model.player.PlayerEntity

/**
 * [PlayerModelAbstract] provides automatic delegation of [IPlayerProperties] and demands implementation of relevant
 * related models.
 */
abstract class PlayerModelAbstract(
    entity: PlayerEntity
) : IPlayerProperties by entity, ModelAbstract<PlayerEntity>(entity) {
    val user get() = UserModel.create(entity.user)
    val game get() = GameModel.create(entity.game)
}