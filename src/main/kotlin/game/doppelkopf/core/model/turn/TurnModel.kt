package game.doppelkopf.core.model.turn

import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.persistence.model.turn.TurnEntity

class TurnModel(
    entity: TurnEntity,
    factoryProvider: ModelFactoryProvider
) : TurnModelAbstract(entity, factoryProvider)