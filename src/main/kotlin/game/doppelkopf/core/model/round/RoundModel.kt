package game.doppelkopf.core.model.round

import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.persistence.model.round.RoundEntity

class RoundModel(
    entity: RoundEntity,
    factoryProvider: ModelFactoryProvider
) : RoundModelAbstract(entity, factoryProvider)