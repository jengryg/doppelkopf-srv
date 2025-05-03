package game.doppelkopf.core.model.hand

import game.doppelkopf.common.IModelFactory
import game.doppelkopf.core.model.ModelFactoryCache
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.adapter.persistence.model.hand.HandEntity

class HandModelFactory(
    private val factoryProvider: ModelFactoryProvider
) : IModelFactory<HandEntity, HandModel> {
    private val cache = ModelFactoryCache<HandEntity, HandModel>()

    override fun create(entity: HandEntity): HandModel {
        return cache.getOrPut(entity) { HandModel(entity, factoryProvider) }
    }
}