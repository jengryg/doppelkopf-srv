package game.doppelkopf.persistence.model.game

import game.doppelkopf.persistence.IBaseEntityRepository
import org.springframework.stereotype.Repository

@Repository
interface GameRepository : IBaseEntityRepository<GameEntity>