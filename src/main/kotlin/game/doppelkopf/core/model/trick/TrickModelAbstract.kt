package game.doppelkopf.core.model.trick

import game.doppelkopf.core.cards.Card
import game.doppelkopf.core.common.errors.GameFailedException
import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.hand.IHandModel
import game.doppelkopf.core.model.round.IRoundModel
import game.doppelkopf.persistence.model.trick.TrickEntity

abstract class TrickModelAbstract(
    entity: TrickEntity,
    factoryProvider: ModelFactoryProvider
) : ITrickModel, ITrickProperties by entity, ModelAbstract<TrickEntity>(entity, factoryProvider) {
    override val round: IRoundModel
        get() = factoryProvider.round.create(entity.round)

    override val winner: IHandModel?
        get() = entity.winner?.let { factoryProvider.hand.create(it) }

    override val size: Int
        get() = entity.cards.size

    override val cards: List<Card>
        get() = round.deck.getCards(entity.cards).getOrElse {
            throw GameFailedException("Can not decode the cards of trick $this.", entity.id, it)
        }
}