package game.doppelkopf.core.model.hand

import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.core.model.player.PlayerModel
import game.doppelkopf.core.model.round.RoundModel
import game.doppelkopf.persistence.model.hand.HandEntity

/**
 * [HandModelAbstract] provides automatic delegation of [IHandProperties] and demands implementation of relevant related
 * models.
 */
abstract class HandModelAbstract(
    entity: HandEntity
) : IHandProperties by entity, ModelAbstract<HandEntity>(entity) {
    val round get() = RoundModel.create(entity.round)
    val player get() = PlayerModel.create(entity.player)
}