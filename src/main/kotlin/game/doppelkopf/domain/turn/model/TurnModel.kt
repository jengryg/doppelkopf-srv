package game.doppelkopf.domain.turn.model

import game.doppelkopf.adapter.persistence.model.turn.TurnEntity
import game.doppelkopf.core.model.ModelFactoryProvider

class TurnModel(
    entity: TurnEntity,
    factoryProvider: ModelFactoryProvider
) : TurnModelAbstract(entity, factoryProvider)