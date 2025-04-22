package game.doppelkopf.persistence.model.trick

import game.doppelkopf.core.cards.CardDemand
import game.doppelkopf.core.common.enums.TrickState
import game.doppelkopf.core.model.trick.ITrickProperties
import game.doppelkopf.persistence.model.BaseEntity
import game.doppelkopf.persistence.model.hand.HandEntity
import game.doppelkopf.persistence.model.round.RoundEntity
import game.doppelkopf.persistence.model.turn.TurnEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
class TrickEntity(
    @ManyToOne(
        optional = false,
        fetch = FetchType.EAGER
    )
    val round: RoundEntity,

    @Column
    override val number: Int,

    @Column
    override val openIndex: Int,

    @Column
    override val demand: CardDemand
) : BaseEntity(), ITrickProperties {
    @Column
    override var started: Instant? = null

    @Column
    override var ended: Instant? = null

    @Column
    override var state: TrickState = TrickState.FIRST_CARD_PLAYED

    @Column
    override var leadingCardIndex: Int = 0

    @Column
    override var score: Int = 0

    @ElementCollection
    val cards: MutableList<String> = mutableListOf()

    @ManyToOne(
        fetch = FetchType.EAGER,
        optional = true
    )
    var winner: HandEntity? = null

    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "trick",
        cascade = [CascadeType.ALL],
    )
    val turns = mutableSetOf<TurnEntity>()
}