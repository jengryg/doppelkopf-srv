package game.doppelkopf.domain.turn.service

import game.doppelkopf.adapter.persistence.model.turn.TurnEntity
import game.doppelkopf.common.IModelFactory
import game.doppelkopf.common.ModelFactoryCache
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.turn.model.TurnModel

class TurnModelFactory(
    private val factoryProvider: ModelFactoryProvider,
) : IModelFactory<TurnEntity, TurnModel> {
    private val cache = ModelFactoryCache<TurnEntity, TurnModel>()

    override fun create(entity: TurnEntity): TurnModel {
        return cache.getOrPut(entity) { TurnModel(entity, factoryProvider) }
    }
}