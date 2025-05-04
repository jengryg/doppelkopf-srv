package game.doppelkopf.domain.round.service

import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.common.service.IModelFactory
import game.doppelkopf.common.service.ModelFactoryCache
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.round.model.RoundModel

class RoundModelFactory(
    private val factoryProvider: ModelFactoryProvider
) : IModelFactory<RoundEntity, RoundModel> {
    private val cache = ModelFactoryCache<RoundEntity, RoundModel>()

    override fun create(entity: RoundEntity): RoundModel {
        return cache.getOrPut(entity) { RoundModel(entity, factoryProvider) }
    }
}