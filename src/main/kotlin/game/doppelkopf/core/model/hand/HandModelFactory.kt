package game.doppelkopf.core.model.hand

import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.common.IModelFactory
import game.doppelkopf.core.model.ModelFactoryCache
import game.doppelkopf.core.model.ModelFactoryProvider

class HandModelFactory(
    private val factoryProvider: ModelFactoryProvider
) : IModelFactory<HandEntity, HandModel> {
    private val cache = ModelFactoryCache<HandEntity, HandModel>()

    override fun create(entity: HandEntity): HandModel {
        return cache.getOrPut(entity) { HandModel(entity, factoryProvider) }
    }
}