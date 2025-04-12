package game.doppelkopf.security

import game.doppelkopf.persistence.model.user.UserEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails as IUserDetails

/**
 * [UserDetails] implements spring securities [IUserDetails] interface based on our [UserEntity] class.
 */
class UserDetails(
    val entity: UserEntity
) : IUserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return entity.authorities.map { SimpleGrantedAuthority(it) }.toMutableSet()
    }

    override fun getPassword(): String {
        return entity.password
    }

    override fun getUsername(): String {
        return entity.username
    }

    override fun isAccountNonLocked(): Boolean {
        return !entity.locked
    }

    override fun isEnabled(): Boolean {
        return entity.enabled
    }

    override fun isAccountNonExpired(): Boolean {
        // We currently do not use account expiration.
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        // We currently do not use credentials expiration.
        return true
    }
}