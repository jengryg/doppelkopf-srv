package game.doppelkopf.persistence.play

import game.doppelkopf.core.play.model.RoundContract
import game.doppelkopf.core.play.model.RoundState
import game.doppelkopf.persistence.BaseEntity
import game.doppelkopf.persistence.game.GameEntity
import game.doppelkopf.persistence.game.PlayerEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
class RoundEntity(
    @ManyToOne(
        optional = false,
        fetch = FetchType.EAGER,
    )
    val game: GameEntity,

    @ManyToOne(
        optional = false,
        fetch = FetchType.EAGER,
    )
    val dealer: PlayerEntity,

    @Column
    val number: Int,
) : BaseEntity() {
    @Column
    var started: Instant? = null

    @Column
    var ended: Instant? = null

    @Column
    @Enumerated(EnumType.STRING)
    var state: RoundState = RoundState.INITIALIZED

    @Column
    @Enumerated(EnumType.STRING)
    var type: RoundContract = RoundContract.UNDECIDED
}