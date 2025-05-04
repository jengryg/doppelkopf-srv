package game.doppelkopf.domain.turn.model

import game.doppelkopf.adapter.persistence.model.turn.TurnEntity
import game.doppelkopf.common.IModelFactory
import game.doppelkopf.common.ModelFactoryCache
import game.doppelkopf.domain.ModelFactoryProvider

class TurnModelFactory(
    private val factoryProvider: ModelFactoryProvider,
) : IModelFactory<TurnEntity, TurnModel> {
    private val cache = ModelFactoryCache<TurnEntity, TurnModel>()

    override fun create(entity: TurnEntity): TurnModel {
        return cache.getOrPut(entity) { TurnModel(entity, factoryProvider) }
    }
}