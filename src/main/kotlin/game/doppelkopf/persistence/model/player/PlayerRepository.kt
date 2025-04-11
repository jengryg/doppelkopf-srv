package game.doppelkopf.persistence.model.player

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PlayerRepository : JpaRepository<PlayerEntity, UUID>