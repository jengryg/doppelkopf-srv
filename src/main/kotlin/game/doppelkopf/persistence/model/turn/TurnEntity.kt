package game.doppelkopf.persistence.model.turn

import game.doppelkopf.core.model.turn.ITurnProperties
import game.doppelkopf.persistence.model.BaseEntity
import game.doppelkopf.persistence.model.hand.HandEntity
import game.doppelkopf.persistence.model.round.RoundEntity
import game.doppelkopf.persistence.model.trick.TrickEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class TurnEntity(
    @ManyToOne(
        optional = false,
        fetch = FetchType.EAGER
    )
    val round: RoundEntity,

    @ManyToOne(
        optional = false,
        fetch = FetchType.EAGER
    )
    val hand: HandEntity,

    @ManyToOne(
        optional = false,
        fetch = FetchType.EAGER
    )
    val trick: TrickEntity,

    @Column
    override val number: Int,

    @Column
    val card: String,
) : BaseEntity(), ITurnProperties {

}