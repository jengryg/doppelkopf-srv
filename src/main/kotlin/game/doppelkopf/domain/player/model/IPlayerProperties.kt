package game.doppelkopf.domain.player.model

import game.doppelkopf.common.model.IBaseProperties

interface IPlayerProperties : IBaseProperties {
    /**
     * The number of the seat this player is sitting in.
     */
    val seat: Int

    /**
     * Flag to indicate if this player is currently the dealer.
     */
    var dealer: Boolean

    /**
     * The points of the player.
     */
    var points: Int
}