package game.doppelkopf.domain.call.model

import game.doppelkopf.adapter.persistence.model.call.CallEntity
import game.doppelkopf.domain.ModelFactoryProvider

class CallModel(
    entity: CallEntity,
    factoryProvider: ModelFactoryProvider
) : CallModelAbstract(entity, factoryProvider)