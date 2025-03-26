package game.doppelkopf.persistence.game

import game.doppelkopf.persistence.BaseEntity
import game.doppelkopf.persistence.user.UserEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

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
    val seat: Int,
) : BaseEntity()