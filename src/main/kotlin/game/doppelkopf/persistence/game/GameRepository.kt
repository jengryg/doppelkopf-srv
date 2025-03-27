package game.doppelkopf.persistence.game

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface GameRepository : JpaRepository<GameEntity, UUID>