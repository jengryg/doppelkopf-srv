package game.doppelkopf.domain.trick.service

import game.doppelkopf.adapter.persistence.model.trick.TrickEntity
import game.doppelkopf.common.IModelFactory
import game.doppelkopf.common.ModelFactoryCache
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.trick.model.TrickModel

class TrickModelFactory(
    private val factoryProvider: ModelFactoryProvider
) : IModelFactory<TrickEntity, TrickModel> {
    private val cache = ModelFactoryCache<TrickEntity, TrickModel>()

    override fun create(entity: TrickEntity): TrickModel {
        return cache.getOrPut(entity) { TrickModel(entity, factoryProvider) }
    }
}