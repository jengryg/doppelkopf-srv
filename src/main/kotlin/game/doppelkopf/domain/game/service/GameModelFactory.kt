package game.doppelkopf.domain.game.service

import game.doppelkopf.adapter.persistence.model.game.GameEntity
import game.doppelkopf.common.IModelFactory
import game.doppelkopf.common.ModelFactoryCache
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.game.model.GameModel

class GameModelFactory(
    private val factoryProvider: ModelFactoryProvider
) : IModelFactory<GameEntity, GameModel> {
    private val cache = ModelFactoryCache<GameEntity, GameModel>()

    override fun create(entity: GameEntity): GameModel {
        return cache.getOrPut(entity) { GameModel(entity, factoryProvider) }
    }
}