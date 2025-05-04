package game.doppelkopf.core.model.hand

import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.core.model.ModelFactoryProvider

class HandModel(
    entity: HandEntity,
    factoryProvider: ModelFactoryProvider
) : HandModelAbstract(entity, factoryProvider)