package game.doppelkopf.persistence.model.hand

import game.doppelkopf.core.common.enums.Bidding
import game.doppelkopf.core.common.enums.Declaration
import game.doppelkopf.core.common.enums.Team
import game.doppelkopf.persistence.model.BaseEntity
import game.doppelkopf.persistence.model.player.PlayerEntity
import game.doppelkopf.persistence.model.round.RoundEntity
import jakarta.persistence.*

@Entity
class HandEntity(
    @ManyToOne(
        fetch = FetchType.EAGER,
        optional = false
    )
    val round: RoundEntity,

    @ManyToOne(
        fetch = FetchType.EAGER,
        optional = false
    )
    val player: PlayerEntity,

    @ElementCollection
    val cardsRemaining: MutableList<String>,

    @Column
    val hasMarriage: Boolean,
) : BaseEntity() {
    @ElementCollection
    var cardsPlayed: MutableList<String> = mutableListOf()

    @Column
    var declared: Declaration = Declaration.NOTHING

    @Column
    var bidding: Bidding = Bidding.NOTHING

    @Column
    var isMarried: Boolean = false

    @Column
    var playsSolo: Boolean = false

    /**
     * [internalTeam] should only be used internally and not be communicated to the outside since it could leak
     * information about the game to the players in advance.
     */
    @Column
    var internalTeam: Team = Team.NA

    /**
     * [playerTeam] should only be shown to the [player] of this hand to prevent information leakage about the game to
     * other players.
     */
    @Column
    var playerTeam: Team = Team.NA

    /**
     * [publicTeam] is safe to be shown to all players of the game at any time.
     */
    @Column
    var publicTeam: Team = Team.NA
}