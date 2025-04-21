package game.doppelkopf.core.model.hand

import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.persistence.model.hand.HandEntity

class HandModel(
    entity: HandEntity,
    factoryProvider: ModelFactoryProvider
) : HandModelAbstract(entity, factoryProvider)