package game.doppelkopf.domain.hand.model

import game.doppelkopf.common.IBaseProperties
import game.doppelkopf.domain.hand.enums.Bidding
import game.doppelkopf.domain.hand.enums.Declaration
import game.doppelkopf.domain.hand.enums.Team

interface IHandProperties : IBaseProperties {
    /**
     * The index of the hand determines the order of play.
     */
    val index: Int

    /**
     * Flag to indicate if this hand has a marriage, i.e. both queens of clubs.
     */
    val hasMarriage: Boolean

    /**
     * The [Declaration] this hand made in the declaration phase.
     */
    var declared: Declaration

    /**
     * The [Bidding] this hand made in the bidding phase.
     */
    var bidding: Bidding

    /**
     * Flag to indicate if this hand is in a marriage.
     */
    var isMarried: Boolean

    /**
     * Flag to indicate if this hand plays a solo, including silent marriage.
     */
    var playsSolo: Boolean

    /**
     * [internalTeam] should only be used internally and not be communicated to the outside since it could leak
     * information about the game to the players in advance.
     */
    var internalTeam: Team

    /**
     * [playerTeam] should only be shown to the player of this hand to prevent information leakage about the game to
     * other players.
     */
    var playerTeam: Team

    /**
     * [publicTeam] is safe to be shown to all players of the game at any time.
     */
    var publicTeam: Team
}