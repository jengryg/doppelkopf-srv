package game.doppelkopf.core.model.round

import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.common.IModelFactory
import game.doppelkopf.core.model.ModelFactoryCache
import game.doppelkopf.core.model.ModelFactoryProvider

class RoundModelFactory(
    private val factoryProvider: ModelFactoryProvider
) : IModelFactory<RoundEntity, RoundModel> {
    private val cache = ModelFactoryCache<RoundEntity, RoundModel>()

    override fun create(entity: RoundEntity): RoundModel {
        return cache.getOrPut(entity) { RoundModel(entity, factoryProvider) }
    }
}