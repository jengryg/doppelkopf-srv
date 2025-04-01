package game.doppelkopf.core.play.model

import game.doppelkopf.core.cards.Deck
import game.doppelkopf.persistence.play.RoundEntity
import org.springframework.stereotype.Service

@Service
class RoundModelFactory {
    fun create(roundEntity: RoundEntity): RoundModel {
        return RoundModel(
            roundEntity,
            Deck.create(roundEntity.deck)
        )
    }
}