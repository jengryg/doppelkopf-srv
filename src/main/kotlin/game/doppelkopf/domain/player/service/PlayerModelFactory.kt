package game.doppelkopf.domain.player.service

import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.common.IModelFactory
import game.doppelkopf.common.ModelFactoryCache
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.player.model.PlayerModel

class PlayerModelFactory(
    private val factoryProvider: ModelFactoryProvider
) : IModelFactory<PlayerEntity, PlayerModel> {
    private val cache = ModelFactoryCache<PlayerEntity, PlayerModel>()

    override fun create(entity: PlayerEntity): PlayerModel {
        return cache.getOrPut(entity) { PlayerModel(entity, factoryProvider) }
    }
}