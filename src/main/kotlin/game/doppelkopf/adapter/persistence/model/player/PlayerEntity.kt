package game.doppelkopf.adapter.persistence.model.player

import game.doppelkopf.adapter.persistence.model.BaseEntity
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.domain.player.model.IPlayerProperties
import jakarta.persistence.*

/**
 * [PlayerEntity] represents a specific [user] playing in a specific [game] sitting at [seat] position.
 */
@Suppress("unused")
@Entity
class PlayerEntity(
    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false,
    )
    val user: UserEntity,

    @ManyToOne(
        fetch = FetchType.LAZY,
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

    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "player",
        cascade = [CascadeType.ALL],
    )
    val hands = mutableSetOf<HandEntity>()
}