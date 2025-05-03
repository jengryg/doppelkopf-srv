package game.doppelkopf.core.model.turn

import game.doppelkopf.core.cards.Card
import game.doppelkopf.core.errors.GameFailedException
import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.hand.IHandModel
import game.doppelkopf.core.model.round.IRoundModel
import game.doppelkopf.core.model.trick.ITrickModel
import game.doppelkopf.persistence.model.turn.TurnEntity

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