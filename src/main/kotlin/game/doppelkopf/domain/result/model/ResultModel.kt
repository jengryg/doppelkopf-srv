package game.doppelkopf.domain.result.model

import game.doppelkopf.adapter.persistence.model.result.ResultEntity
import game.doppelkopf.domain.ModelFactoryProvider

class ResultModel(
    entity: ResultEntity,
    factoryProvider: ModelFactoryProvider
) : ResultModelAbstract(entity, factoryProvider)