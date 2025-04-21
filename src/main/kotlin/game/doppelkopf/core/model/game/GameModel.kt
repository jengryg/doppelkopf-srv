package game.doppelkopf.core.model.game

import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.persistence.model.game.GameEntity

class GameModel(
    entity: GameEntity,
    factoryProvider: ModelFactoryProvider
) : GameModelAbstract(entity, factoryProvider)