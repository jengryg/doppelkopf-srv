package game.doppelkopf.security

import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import game.doppelkopf.persistence.user.UserRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.security.core.userdetails.UserDetails as IUserDetails
import org.springframework.security.core.userdetails.UserDetailsService as IUserDetailsService

/**
 * Provides the method [loadUserByUsername] to spring security.
 * The [IUserDetails] returned by this method is used to determine if the user is authorized or not.
 *
 * See [UserDetails] for the corresponding interface implementation in this application,
 *
 * @property userRepository the repository to search the user in
 */
@Service
class UserDetailsService(
    private val userRepository: UserRepository
) : IUserDetailsService, Logging {
    private val log = logger()

    /**
     * @param username the name of the user to load
     * @return the [UserDetails] corresponding to the user if it exists in the [userRepository]
     */
    override fun loadUserByUsername(username: String?): IUserDetails {
        username ?: throw UsernameNotFoundException("Username can not be null.")

        return userRepository.findByUsername(username)?.let {
            log.atDebug()
                .setMessage("Found UserEntity in database.")
                .addKeyValue("username") { username }
                .addKeyValue("id") { it.id }
                .log()

            UserDetails(it)
        } ?: throw UsernameNotFoundException("Username $username not found.")
    }
}