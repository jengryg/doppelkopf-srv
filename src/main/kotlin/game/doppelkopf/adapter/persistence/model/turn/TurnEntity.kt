package game.doppelkopf.adapter.persistence.model.turn

import game.doppelkopf.adapter.persistence.model.BaseEntity
import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.trick.TrickEntity
import game.doppelkopf.core.model.turn.ITurnProperties
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class TurnEntity(
    @ManyToOne(
        optional = false,
        fetch = FetchType.LAZY
    )
    val round: RoundEntity,

    @ManyToOne(
        optional = false,
        fetch = FetchType.LAZY
    )
    val hand: HandEntity,

    @ManyToOne(
        optional = false,
        fetch = FetchType.LAZY
    )
    val trick: TrickEntity,

    @Column
    override val number: Int,

    @Column
    val card: String,
) : BaseEntity(), ITurnProperties