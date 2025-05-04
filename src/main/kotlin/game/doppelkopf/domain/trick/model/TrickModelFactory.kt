package game.doppelkopf.domain.trick.model

import game.doppelkopf.adapter.persistence.model.trick.TrickEntity
import game.doppelkopf.common.IModelFactory
import game.doppelkopf.core.model.ModelFactoryCache
import game.doppelkopf.core.model.ModelFactoryProvider

class TrickModelFactory(
    private val factoryProvider: ModelFactoryProvider
) : IModelFactory<TrickEntity, TrickModel> {
    private val cache = ModelFactoryCache<TrickEntity, TrickModel>()

    override fun create(entity: TrickEntity): TrickModel {
        return cache.getOrPut(entity) { TrickModel(entity, factoryProvider) }
    }
}