package game.doppelkopf.adapter.persistence.model.user

import com.fasterxml.jackson.annotation.JsonIgnore
import game.doppelkopf.adapter.persistence.model.BaseEntity
import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.domain.user.enums.Authority
import game.doppelkopf.domain.user.model.IUserProperties
import jakarta.persistence.*

@Suppress("unused")
@Entity
class UserEntity(
    @Column(unique = true)
    override var username: String,

    @Column
    @JsonIgnore
    var password: String,

    @Column
    @JsonIgnore
    var enabled: Boolean = true,

    @Column
    @JsonIgnore
    var locked: Boolean = false,

    @Enumerated(EnumType.STRING)
    @JsonIgnore
    var authority: Authority = Authority.NONE
) : BaseEntity(), IUserProperties {
    /**
     * Simple authority is wrapped inside a set for further processing and modifications.
     */
    val authorities: Set<String> get() = if (authority == Authority.NONE) emptySet() else setOf(authority.authority)

    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "creator",
        cascade = [CascadeType.ALL],
    )
    val createdGames = mutableSetOf<GameEntity>()

    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "user",
        cascade = [CascadeType.ALL],
    )
    val players = mutableSetOf<PlayerEntity>()
}