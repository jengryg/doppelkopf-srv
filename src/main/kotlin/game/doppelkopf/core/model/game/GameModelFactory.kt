package game.doppelkopf.core.model.game

import game.doppelkopf.core.model.IModelFactory
import game.doppelkopf.persistence.model.game.GameEntity
import org.springframework.stereotype.Service

@Service
class GameModelFactory : IModelFactory<GameEntity, GameModel> {
    override fun create(entity: GameEntity): GameModel {
        return GameModel(entity)
    }
}