package game.doppelkopf.persistence.play

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RoundRepository : JpaRepository<RoundEntity, UUID>