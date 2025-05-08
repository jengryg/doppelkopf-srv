package game.doppelkopf.domain.result.service

import game.doppelkopf.adapter.persistence.model.result.ResultEntity
import game.doppelkopf.common.service.IModelFactory
import game.doppelkopf.common.service.ModelFactoryCache
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.result.model.ResultModel

class ResultModelFactory(
    private val factoryProvider: ModelFactoryProvider
) : IModelFactory<ResultEntity, ResultModel> {
    private val cache = ModelFactoryCache<ResultEntity, ResultModel>()

    override fun create(entity: ResultEntity): ResultModel {
        return cache.getOrPut(entity) { ResultModel(entity, factoryProvider) }
    }
}