package game.doppelkopf.persistence.model.user

import game.doppelkopf.persistence.IBaseEntityRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : IBaseEntityRepository<UserEntity> {
    fun findByUsername(username: String): UserEntity?
}