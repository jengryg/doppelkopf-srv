package game.doppelkopf.persistence.model.hand

import game.doppelkopf.core.common.enums.Bidding
import game.doppelkopf.core.common.enums.Declaration
import game.doppelkopf.core.common.enums.Team
import game.doppelkopf.core.model.hand.IHandProperties
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

    @Column
    override val index: Int,

    @ElementCollection
    val cardsRemaining: MutableList<String>,

    @Column
    override val hasMarriage: Boolean
) : BaseEntity(), IHandProperties {
    @ElementCollection
    var cardsPlayed: MutableList<String> = mutableListOf()

    @Column
    override var declared: Declaration = Declaration.NOTHING

    @Column
    override var bidding: Bidding = Bidding.NOTHING

    @Column
    override var isMarried: Boolean = false

    @Column
    override var playsSolo: Boolean = false

    @Column
    override var internalTeam: Team = Team.NA

    @Column
    override var playerTeam: Team = Team.NA

    @Column
    override var publicTeam: Team = Team.NA
}