package game.doppelkopf.persistence.model.hand

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface HandRepository : JpaRepository<HandEntity, UUID>