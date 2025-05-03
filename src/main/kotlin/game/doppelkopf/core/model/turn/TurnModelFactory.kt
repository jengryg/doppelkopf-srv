package game.doppelkopf.core.model.turn

import game.doppelkopf.common.IModelFactory
import game.doppelkopf.core.model.ModelFactoryCache
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.persistence.model.turn.TurnEntity

class TurnModelFactory(
    private val factoryProvider: ModelFactoryProvider,
) : IModelFactory<TurnEntity, TurnModel> {
    private val cache = ModelFactoryCache<TurnEntity, TurnModel>()

    override fun create(entity: TurnEntity): TurnModel {
        return cache.getOrPut(entity) { TurnModel(entity, factoryProvider) }
    }
}