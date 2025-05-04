package game.doppelkopf.core.model.game

import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.core.model.ModelFactoryProvider

class GameModel(
    entity: GameEntity,
    factoryProvider: ModelFactoryProvider
) : GameModelAbstract(entity, factoryProvider)