package game.doppelkopf.adapter.persistence.model.round

import game.doppelkopf.core.cards.DeckMode
import game.doppelkopf.core.common.enums.RoundContract
import game.doppelkopf.core.common.enums.RoundState
import game.doppelkopf.core.model.round.IRoundProperties
import game.doppelkopf.adapter.persistence.BaseEntity
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.adapter.persistence.model.trick.TrickEntity
import game.doppelkopf.adapter.persistence.model.turn.TurnEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
class RoundEntity(
    @ManyToOne(
        optional = false,
        fetch = FetchType.LAZY,
    )
    val game: GameEntity,

    @ManyToOne(
        optional = false,
        fetch = FetchType.LAZY,
    )
    val dealer: PlayerEntity,

    @Column
    override val number: Int,
) : BaseEntity(), IRoundProperties {
    @Column
    override var started: Instant? = null

    @Column
    override var ended: Instant? = null

    @Column
    @Enumerated(EnumType.STRING)
    override var state: RoundState = RoundState.WAITING_FOR_DECLARATIONS

    @Column
    @Enumerated(EnumType.STRING)
    override var contract: RoundContract = RoundContract.UNDECIDED

    @Column
    @Enumerated(EnumType.STRING)
    override var deckMode: DeckMode = DeckMode.DIAMONDS

    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "round",
        cascade = [CascadeType.ALL],
    )
    val hands = mutableSetOf<HandEntity>()

    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "round",
        cascade = [CascadeType.ALL],
    )
    val tricks = mutableSetOf<TrickEntity>()

    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "round",
        cascade = [CascadeType.ALL],
    )
    val turns = mutableSetOf<TurnEntity>()
}