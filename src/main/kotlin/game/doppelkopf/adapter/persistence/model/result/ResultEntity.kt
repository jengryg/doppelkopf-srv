package game.doppelkopf.adapter.persistence.model.result

import game.doppelkopf.adapter.persistence.model.BaseEntity
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.domain.hand.enums.DefiniteTeam
import game.doppelkopf.domain.result.model.IResultProperties
import jakarta.persistence.*

@Entity
class ResultEntity(
    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false,
    )
    val round: RoundEntity,

    @Column
    @Enumerated(EnumType.STRING)
    override val team: DefiniteTeam,

    @Column
    override val trickCount: Int,

    @Column
    override val score: Int,

    @Column
    override val target: Int,

    @Column
    override val pointsForWinning: Int,

    @Column
    override val pointsLostScore90: Int,

    @Column
    override val pointsLostScore60: Int,

    @Column
    override val pointsLostScore30: Int,

    @Column
    override val pointsLostScore00: Int,

    @Column
    override val pointsBasicCallsRe: Int,

    @Column
    override val pointsBasicCallsKo: Int,

    @Column
    override val pointsUnderCallsRe90: Int,

    @Column
    override val pointsUnderCallsKo90: Int,

    @Column
    override val pointsUnderCallsRe60: Int,

    @Column
    override val pointsUnderCallsKo60: Int,

    @Column
    override val pointsUnderCallsRe30: Int,

    @Column
    override val pointsUnderCallsKo30: Int,

    @Column
    override val pointsUnderCallsRe00: Int,

    @Column
    override val pointsUnderCallsKo00: Int,

    @Column
    override val pointsBeatingRe90: Int,

    @Column
    override val pointsBeatingKo90: Int,

    @Column
    override val pointsBeatingRe60: Int,

    @Column
    override val pointsBeatingKo60: Int,

    @Column
    override val pointsBeatingRe30: Int,

    @Column
    override val pointsBeatingKo30: Int,

    @Column
    override val pointsBeatingRe00: Int,

    @Column
    override val pointsBeatingKo00: Int,

    @Column
    override val pointsForOpposition: Int,

    @Column
    override val pointsForDoppelkopf: Int,

    @Column
    override val pointsForCharly: Int
) : BaseEntity(), IResultProperties