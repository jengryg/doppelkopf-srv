package game.doppelkopf.persistence.game

import game.doppelkopf.persistence.user.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PlayerRepository : JpaRepository<PlayerEntity, UUID> {
    fun findByUser(user: UserEntity) : List<PlayerEntity>
}