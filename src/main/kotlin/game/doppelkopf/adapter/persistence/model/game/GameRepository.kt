package game.doppelkopf.adapter.persistence.model.game

import game.doppelkopf.adapter.persistence.IBaseEntityRepository
import org.springframework.stereotype.Repository

@Repository
interface GameRepository : IBaseEntityRepository<GameEntity>