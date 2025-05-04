package game.doppelkopf.adapter.persistence.model.user

import game.doppelkopf.adapter.persistence.model.IBaseEntityRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : IBaseEntityRepository<UserEntity> {
    fun findByUsername(username: String): UserEntity?
}