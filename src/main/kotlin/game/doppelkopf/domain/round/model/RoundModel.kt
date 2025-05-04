package game.doppelkopf.domain.round.model

import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.core.model.ModelFactoryProvider

class RoundModel(
    entity: RoundEntity,
    factoryProvider: ModelFactoryProvider
) : RoundModelAbstract(entity, factoryProvider)