package game.doppelkopf.adapter.persistence.model.hand

import game.doppelkopf.adapter.persistence.model.IBaseEntityRepository
import org.springframework.stereotype.Repository

@Repository
interface HandRepository : IBaseEntityRepository<HandEntity> {

}