package game.doppelkopf.persistence.model.player

import game.doppelkopf.persistence.IBaseEntityRepository
import org.springframework.stereotype.Repository

@Repository
interface PlayerRepository : IBaseEntityRepository<PlayerEntity>