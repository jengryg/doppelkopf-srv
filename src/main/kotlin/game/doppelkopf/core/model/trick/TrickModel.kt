package game.doppelkopf.core.model.trick

import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.adapter.persistence.model.trick.TrickEntity

class TrickModel(
    entity: TrickEntity,
    factoryProvider: ModelFactoryProvider
) : TrickModelAbstract(entity, factoryProvider)