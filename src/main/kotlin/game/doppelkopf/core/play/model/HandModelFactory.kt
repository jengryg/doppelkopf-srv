package game.doppelkopf.core.play.model

import game.doppelkopf.persistence.model.hand.HandEntity
import org.springframework.stereotype.Service

@Service
class HandModelFactory {
    fun create(hand: HandEntity): HandModel {
        return HandModel(hand)
    }
}