package game.doppelkopf.adapter.persistence.model.call

import game.doppelkopf.adapter.persistence.model.BaseEntity
import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.domain.call.enums.CallType
import game.doppelkopf.domain.call.model.ICallProperties
import jakarta.persistence.*

@Entity
class CallEntity(
    @ManyToOne(
        optional = false,
        fetch = FetchType.LAZY
    )
    val hand: HandEntity,

    @Column
    @Enumerated(EnumType.STRING)
    override val callType: CallType,

    @Column
    override val cardsPlayedBefore: Int,
) : BaseEntity(), ICallProperties