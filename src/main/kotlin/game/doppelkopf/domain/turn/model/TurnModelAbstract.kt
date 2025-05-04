package game.doppelkopf.domain.turn.model

import game.doppelkopf.adapter.persistence.model.turn.TurnEntity
import game.doppelkopf.domain.deck.model.Card
import game.doppelkopf.common.errors.GameFailedException
import game.doppelkopf.domain.ModelAbstract
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.hand.model.IHandModel
import game.doppelkopf.domain.round.model.IRoundModel
import game.doppelkopf.domain.trick.model.ITrickModel

abstract class TurnModelAbstract(
    entity: TurnEntity,
    factoryProvider: ModelFactoryProvider
) : ITurnModel, ITurnProperties by entity, ModelAbstract<TurnEntity>(entity, factoryProvider) {
    override val round: IRoundModel
        get() = factoryProvider.round.create(entity.round)

    override val hand: IHandModel
        get() = factoryProvider.hand.create(entity.hand)

    override val trick: ITrickModel
        get() = factoryProvider.trick.create(entity.trick)

    override val card: Card
        get() = round.deck.getCard(entity.card).getOrElse {
            throw GameFailedException("Can not decode the card of the turn $this.", entity.id, it)
        }
}