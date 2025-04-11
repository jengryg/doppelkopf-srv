package game.doppelkopf.persistence.model.player

import game.doppelkopf.core.model.player.IPlayerProperties
import game.doppelkopf.persistence.model.BaseEntity
import game.doppelkopf.persistence.model.game.GameEntity
import game.doppelkopf.persistence.model.round.RoundEntity
import game.doppelkopf.persistence.model.user.UserEntity
import jakarta.persistence.*

/**
 * [PlayerEntity] represents a specific [user] playing in a specific [game] sitting at [seat] position.
 */
@Suppress("unused")
@Entity
class PlayerEntity(
    @ManyToOne(
        fetch = FetchType.EAGER,
        optional = false,
    )
    val user: UserEntity,

    @ManyToOne(
        fetch = FetchType.EAGER,
        optional = false
    )
    val game: GameEntity,

    @Column
    override val seat: Int,
) : BaseEntity(), IPlayerProperties {
    @Column
    override var dealer: Boolean = false

    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "dealer",
        cascade = [CascadeType.ALL],
    )
    val dealtRounds = mutableSetOf<RoundEntity>()
}