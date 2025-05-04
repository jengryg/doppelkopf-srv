package game.doppelkopf.domain.game.model

import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.common.IModelFactory
import game.doppelkopf.core.model.ModelFactoryCache
import game.doppelkopf.core.model.ModelFactoryProvider

class GameModelFactory(
    private val factoryProvider: ModelFactoryProvider
) : IModelFactory<GameEntity, GameModel> {
    private val cache = ModelFactoryCache<GameEntity, GameModel>()

    override fun create(entity: GameEntity): GameModel {
        return cache.getOrPut(entity) { GameModel(entity, factoryProvider) }
    }
}