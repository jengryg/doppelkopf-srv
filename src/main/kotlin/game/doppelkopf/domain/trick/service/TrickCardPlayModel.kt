package game.doppelkopf.domain.trick.service

import game.doppelkopf.adapter.persistence.model.trick.TrickEntity
import game.doppelkopf.domain.deck.model.Card
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.trick.model.TrickModelAbstract

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