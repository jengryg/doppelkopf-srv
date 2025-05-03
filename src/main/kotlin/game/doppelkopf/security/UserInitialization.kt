package game.doppelkopf.security

import game.doppelkopf.CommonConfig
import game.doppelkopf.instrumentation.logging.Logging
import game.doppelkopf.instrumentation.logging.logger
import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.adapter.persistence.model.user.UserRepository
import jakarta.transaction.Transactional
import org.bouncycastle.util.encoders.Base64
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.security.SecureRandom

@Component
class UserInitialization(
    private val commonConfig: CommonConfig,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : Logging {
    private val log = logger()

    /**
     * Initialize the security system by creating an `admin` and `user` account with random generated password.
     * These passwords are only exposed if [CommonConfig.stage] is set to `temp`, in this case the password is printed
     * to the logs. In all other cases, the password is not exposed.
     */
    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun initialize() {
        createDefaultAccount("admin", Authority.ADMIN)
        createDefaultAccount("user", Authority.USER)
    }

    /**
     * Create an account with the given username if it does not exist.
     * Assign the given [authority] to the user.
     *
     * If [CommonConfig.stage] is set to `temp` the account will be enabled and the password is printed in the logs,
     * otherwise the account is locked and the password is not exposed.
     */
    private fun createDefaultAccount(username: String, authority: Authority) {
        val exists = userRepository.findByUsername(username)

        if (exists == null) {
            userRepository.save(
                UserEntity(
                    username = username,
                    password = passwordEncoder.encode(
                        Base64.toBase64String(
                            ByteArray(32).also { SecureRandom.getInstanceStrong().nextBytes(it) }
                        ).also {
                            if (commonConfig.stage == "temp") {
                                log.atInfo()
                                    .setMessage("Generated password for initialization user.")
                                    .addKeyValue("username") { username }
                                    .addKeyValue("password") { it }
                                    .log()
                                // only expose the random generated password if the stage is temp,
                                // where the database is re-created on every application start
                            }
                        }
                    ),
                    enabled = commonConfig.stage == "temp",
                    // these user accounts are only available on the temp stage
                    authority = authority
                )
            )
        }
    }
}