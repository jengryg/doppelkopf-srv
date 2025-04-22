package game.doppelkopf.core.model.trick

import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.persistence.model.trick.TrickEntity

abstract class TrickModelAbstract(
    entity: TrickEntity,
    factoryProvider: ModelFactoryProvider
) : ITrickModel, ITrickProperties by entity, ModelAbstract<TrickEntity>(entity, factoryProvider) {
}