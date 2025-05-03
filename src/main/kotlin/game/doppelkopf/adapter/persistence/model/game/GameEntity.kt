package game.doppelkopf.adapter.persistence.model.game

import game.doppelkopf.core.common.enums.GameState
import game.doppelkopf.core.model.game.IGameProperties
import game.doppelkopf.adapter.persistence.BaseEntity
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import jakarta.persistence.*
import java.time.Instant

/**
 * [GameEntity] represents a single game with multiple players that can contain multiple rounds of Doppelkopf.
 */
@Suppress("unused")
@Entity
class GameEntity(
    @ManyToOne(
        optional = false,
        fetch = FetchType.LAZY,
    )
    val creator: UserEntity,

    @Column
    override val maxNumberOfPlayers: Int,
) : BaseEntity(), IGameProperties {
    @Column
    override var started: Instant? = null

    @Column
    override var ended: Instant? = null

    @Column
    @Enumerated(EnumType.STRING)
    override var state: GameState = GameState.INITIALIZED

    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "game",
        cascade = [CascadeType.ALL],
    )
    val players = mutableSetOf<PlayerEntity>()

    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "game",
        cascade = [CascadeType.ALL],
    )
    val rounds = mutableSetOf<RoundEntity>()
}