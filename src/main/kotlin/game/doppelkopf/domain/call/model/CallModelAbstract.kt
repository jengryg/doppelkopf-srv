package game.doppelkopf.domain.call.model

import game.doppelkopf.adapter.persistence.model.call.CallEntity
import game.doppelkopf.domain.ModelAbstract
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.hand.model.IHandModel

abstract class CallModelAbstract(
    entity: CallEntity,
    factoryProvider: ModelFactoryProvider
) : ICallModel, ICallProperties by entity, ModelAbstract<CallEntity>(entity, factoryProvider) {
    override val hand: IHandModel
        get() = factoryProvider.hand.create(entity.hand)
}