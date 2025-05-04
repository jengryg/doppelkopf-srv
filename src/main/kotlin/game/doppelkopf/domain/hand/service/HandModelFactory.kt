package game.doppelkopf.domain.hand.service

import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.common.IModelFactory
import game.doppelkopf.common.ModelFactoryCache
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.hand.model.HandModel

class HandModelFactory(
    private val factoryProvider: ModelFactoryProvider
) : IModelFactory<HandEntity, HandModel> {
    private val cache = ModelFactoryCache<HandEntity, HandModel>()

    override fun create(entity: HandEntity): HandModel {
        return cache.getOrPut(entity) { HandModel(entity, factoryProvider) }
    }
}