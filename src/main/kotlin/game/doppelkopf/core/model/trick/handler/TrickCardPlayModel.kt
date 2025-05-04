package game.doppelkopf.core.model.trick.handler

import game.doppelkopf.adapter.persistence.model.trick.TrickEntity
import game.doppelkopf.core.cards.Card
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.trick.TrickModelAbstract

class TrickCardPlayModel(
    entity: TrickEntity,
    factoryProvider: ModelFactoryProvider
) : TrickModelAbstract(
    entity,
    factoryProvider
) {
    fun playCard(card: Card) {
        entity.cards.add(card.encoded)

        // update the cached values of this trick
        updateCachedValues()
    }
}