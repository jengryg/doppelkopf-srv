package game.doppelkopf.core.model.turn

import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.persistence.model.turn.TurnEntity

abstract class TurnModelAbstract(
    entity: TurnEntity,
    factoryProvider: ModelFactoryProvider
) : ITurnModel, ITurnProperties by entity, ModelAbstract<TurnEntity>(entity, factoryProvider) {
}