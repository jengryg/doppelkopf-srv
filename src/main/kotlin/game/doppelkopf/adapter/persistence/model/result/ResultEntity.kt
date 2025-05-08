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
    override val pointsForOpposition: Int,

    @Column
    override val pointsForScore090: Int,

    @Column
    override val pointsForScore060: Int,

    @Column
    override val pointsForScore030: Int,

    @Column
    override val pointsForScore000: Int,

    @Column
    override val pointsForDoppelkopf: Int,

    @Column
    override val pointsForCharly: Int,
) : BaseEntity(), IResultProperties