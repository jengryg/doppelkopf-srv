package game.doppelkopf.adapter.persistence.model.user

import game.doppelkopf.adapter.persistence.errors.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserPersistence(
    private val userRepository: UserRepository
) {
    fun load(id: UUID): UserEntity {
        return userRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException.forEntity<UserEntity>(id)
    }
}