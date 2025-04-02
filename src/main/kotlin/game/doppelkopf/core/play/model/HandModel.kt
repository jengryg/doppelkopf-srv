package game.doppelkopf.core.play.model

import game.doppelkopf.core.play.enums.Bidding
import game.doppelkopf.core.play.enums.Declaration
import game.doppelkopf.persistence.play.HandEntity

class HandModel(
    val hand: HandEntity
) {
    fun declare(declaration: Declaration) {
        TODO()
    }

    fun bid(bid: Bidding) {
        TODO()
    }
}