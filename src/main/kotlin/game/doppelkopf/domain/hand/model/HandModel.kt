package game.doppelkopf.domain.hand.model

import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.core.model.ModelFactoryProvider

class HandModel(
    entity: HandEntity,
    factoryProvider: ModelFactoryProvider
) : HandModelAbstract(entity, factoryProvider)