package game.doppelkopf.core.model.game

import game.doppelkopf.common.IModelFactory
import game.doppelkopf.core.model.ModelFactoryCache
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.persistence.model.game.GameEntity

class GameModelFactory(
    private val factoryProvider: ModelFactoryProvider
) : IModelFactory<GameEntity, GameModel> {
    private val cache = ModelFactoryCache<GameEntity, GameModel>()

    override fun create(entity: GameEntity): GameModel {
        return cache.getOrPut(entity) { GameModel(entity, factoryProvider) }
    }
}