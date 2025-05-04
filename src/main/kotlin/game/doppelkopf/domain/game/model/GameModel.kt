package game.doppelkopf.domain.game.model

import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.domain.ModelFactoryProvider

class GameModel(
    entity: GameEntity,
    factoryProvider: ModelFactoryProvider
) : GameModelAbstract(entity, factoryProvider)