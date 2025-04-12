package game.doppelkopf.core.model.round

import game.doppelkopf.core.model.IModelFactory
import game.doppelkopf.persistence.model.round.RoundEntity
import org.springframework.stereotype.Service

@Service
class RoundModelFactory : IModelFactory<RoundEntity, RoundModel> {
    override fun create(entity: RoundEntity): RoundModel {
        return RoundModel(entity)
    }
}