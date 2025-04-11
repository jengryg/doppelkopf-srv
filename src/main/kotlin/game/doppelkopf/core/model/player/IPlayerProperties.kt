package game.doppelkopf.core.model.player

import game.doppelkopf.core.common.IBaseProperties

interface IPlayerProperties : IBaseProperties {
    /**
     * The number of the seat this player is sitting in.
     */
    val seat: Int

    /**
     * Flag to indicate if this player is currently the dealer.
     */
    var dealer: Boolean
}