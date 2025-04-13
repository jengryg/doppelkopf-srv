package game.doppelkopf.persistence.model.game

import game.doppelkopf.persistence.BaseEntityRepository
import org.springframework.stereotype.Repository

@Repository
interface GameRepository : BaseEntityRepository<GameEntity>