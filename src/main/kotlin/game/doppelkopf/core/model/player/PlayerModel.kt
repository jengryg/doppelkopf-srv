package game.doppelkopf.core.model.player

import game.doppelkopf.core.model.IModelFactory
import game.doppelkopf.persistence.model.player.PlayerEntity
import java.util.*

class PlayerModel private constructor(
    entity: PlayerEntity
) : PlayerModelAbstract(entity) {
    companion object : IModelFactory<PlayerEntity, PlayerModel> {
        private val instances = mutableMapOf<UUID, PlayerModel>()

        override fun create(entity: PlayerEntity): PlayerModel {
            return instances.getOrPut(entity.id) { PlayerModel(entity) }
        }
    }
}