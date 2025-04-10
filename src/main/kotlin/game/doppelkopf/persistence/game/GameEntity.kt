package game.doppelkopf.persistence.game

import game.doppelkopf.core.common.enums.GameState
import game.doppelkopf.persistence.BaseEntity
import game.doppelkopf.persistence.play.RoundEntity
import game.doppelkopf.persistence.user.UserEntity
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
        fetch = FetchType.EAGER,
    )
    val creator: UserEntity,

    @Column
    val maxNumberOfPlayers: Int,
) : BaseEntity() {
    @Column
    var started: Instant? = null

    @Column
    var ended: Instant? = null

    @Column
    @Enumerated(EnumType.STRING)
    var state: GameState = GameState.INITIALIZED

    @OneToMany(
        fetch = FetchType.EAGER,
        mappedBy = "game",
        cascade = [CascadeType.ALL],
    )
    val players = mutableSetOf<PlayerEntity>()

    @OneToMany(
        fetch = FetchType.EAGER,
        mappedBy = "game",
        cascade = [CascadeType.ALL],
    )
    val rounds = mutableSetOf<RoundEntity>()

    fun getPlayerOfOrNull(user: UserEntity): PlayerEntity? {
        return players.singleOrNull { it.user == user }
    }

    fun getLatestRoundOrNull(): RoundEntity? {
        return rounds.maxByOrNull { it.number }
    }
}