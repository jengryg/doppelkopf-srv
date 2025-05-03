package game.doppelkopf.core.model.player

import game.doppelkopf.common.IModelFactory
import game.doppelkopf.core.model.ModelFactoryCache
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity

class PlayerModelFactory(
    private val factoryProvider: ModelFactoryProvider
) : IModelFactory<PlayerEntity, PlayerModel> {
    private val cache = ModelFactoryCache<PlayerEntity, PlayerModel>()

    override fun create(entity: PlayerEntity): PlayerModel {
        return cache.getOrPut(entity) { PlayerModel(entity, factoryProvider) }
    }
}