package game.doppelkopf.persistence.play

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface RoundRepository : JpaRepository<RoundEntity, UUID>