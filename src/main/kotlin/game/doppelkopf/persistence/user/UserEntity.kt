package game.doppelkopf.persistence.user

import com.fasterxml.jackson.annotation.JsonIgnore
import game.doppelkopf.persistence.BaseEntity
import game.doppelkopf.security.Authority
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Entity
class UserEntity(
    @Column(unique = true)
    var username: String,

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
) : BaseEntity() {
    /**
     * Simple authority is wrapped inside a set for further processing and modifications.
     */
    val authorities: Set<String> get() = if (authority == Authority.NONE) emptySet() else setOf(authority.authority)
}