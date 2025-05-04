package game.doppelkopf.domain.trick.model

import game.doppelkopf.adapter.persistence.model.trick.TrickEntity
import game.doppelkopf.core.model.ModelFactoryProvider

class TrickModel(
    entity: TrickEntity,
    factoryProvider: ModelFactoryProvider
) : TrickModelAbstract(entity, factoryProvider)