package game.doppelkopf.core.model.hand

import game.doppelkopf.core.model.IModelFactory
import game.doppelkopf.persistence.model.hand.HandEntity
import org.springframework.stereotype.Service

@Service
class HandModelFactory : IModelFactory<HandEntity, HandModel> {
    override fun create(entity: HandEntity): HandModel {
        return HandModel(entity)
    }
}