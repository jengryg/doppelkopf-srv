package game.doppelkopf.persistence.model.user

import game.doppelkopf.persistence.BaseEntityRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : BaseEntityRepository<UserEntity> {
    fun findByUsername(username: String): UserEntity?
}