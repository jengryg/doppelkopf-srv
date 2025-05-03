package game.doppelkopf.adapter.persistence.model.player

import game.doppelkopf.adapter.persistence.IBaseEntityRepository
import org.springframework.stereotype.Repository

@Repository
interface PlayerRepository : IBaseEntityRepository<PlayerEntity>