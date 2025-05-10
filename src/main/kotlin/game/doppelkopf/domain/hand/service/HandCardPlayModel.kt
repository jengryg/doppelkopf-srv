package game.doppelkopf.domain.hand.service

import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.common.errors.ofInvalidAction
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.deck.enums.CardDemand
import game.doppelkopf.domain.deck.model.Card
import game.doppelkopf.domain.hand.model.HandModelAbstract
import org.springframework.lang.CheckReturnValue

class HandCardPlayModel(
    entity: HandEntity,
    factoryProvider: ModelFactoryProvider
) : HandModelAbstract(entity, factoryProvider) {
    /**
     * @return true when the card can be played by this hand, false otherwise
     */
    @CheckReturnValue
    fun playCard(card: Card, demand: CardDemand): Result<Unit> {
        if (!entity.cardsRemaining.contains(card.encoded)) {
            return Result.ofInvalidAction("The hand does not contain the card $card.")
        }

        if (!isLegalCardPlay(card, demand)) {
            return Result.ofInvalidAction("You are not allowed to play $card into a trick that demands $demand when your hand can serve the demand.")
        }

        if(!entity.cardsRemaining.remove(card.encoded)) {
            return Result.ofInvalidAction("Could not remove the card $card from the hand.")
        }
        entity.cardsPlayed.add(card.encoded)

        return Result.success(Unit)
    }

    /**
     * @return
     */
    private fun isLegalCardPlay(card: Card, demand: CardDemand): Boolean {
        if (demand == card.demand) {
            // satisfying the demand is always a legal play
            return true
        }

        // Not satisfying the demand is only allowed, if there is
        return cards.all { it.demand != demand }
    }
}