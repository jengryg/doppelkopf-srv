package game.doppelkopf.adapter.persistence.model.trick

import game.doppelkopf.core.cards.CardDemand
import game.doppelkopf.core.common.enums.TrickState
import game.doppelkopf.core.model.trick.ITrickProperties
import game.doppelkopf.adapter.persistence.BaseEntity
import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.turn.TurnEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
class TrickEntity(
    @ManyToOne(
        optional = false,
        fetch = FetchType.LAZY
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
        fetch = FetchType.LAZY,
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