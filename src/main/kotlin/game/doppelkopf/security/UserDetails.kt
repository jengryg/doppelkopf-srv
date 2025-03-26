package game.doppelkopf.security

import game.doppelkopf.persistence.user.UserEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails as IUserDetails

class UserDetails(
    val user: UserEntity
) : IUserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return user.authorities.map { SimpleGrantedAuthority(it) }.toMutableSet()
    }

    override fun getPassword(): String {
        return user.password
    }

    override fun getUsername(): String {
        return user.username
    }

    override fun isAccountNonLocked(): Boolean {
        return !user.locked
    }

    override fun isEnabled(): Boolean {
        return user.enabled
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