package game.doppelkopf.domain.call.service

import game.doppelkopf.adapter.persistence.model.call.CallEntity
import game.doppelkopf.common.service.IModelFactory
import game.doppelkopf.common.service.ModelFactoryCache
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.call.model.CallModel

class CallModelFactory(
    private val factoryProvider: ModelFactoryProvider
) : IModelFactory<CallEntity, CallModel> {
    private val cache = ModelFactoryCache<CallEntity, CallModel>()

    override fun create(entity: CallEntity): CallModel {
        return cache.getOrPut(entity) { CallModel(entity, factoryProvider) }
    }
}