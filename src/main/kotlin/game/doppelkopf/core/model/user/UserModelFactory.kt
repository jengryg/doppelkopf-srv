package game.doppelkopf.core.model.user

import game.doppelkopf.core.model.IModelFactory
import game.doppelkopf.core.model.ModelFactoryCache
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.persistence.model.user.UserEntity

class UserModelFactory(
    private val factoryProvider: ModelFactoryProvider
) : IModelFactory<UserEntity, UserModel> {
    private val cache = ModelFactoryCache<UserEntity, UserModel>()

    override fun create(entity: UserEntity): UserModel {
        return cache.getOrPut(entity) { UserModel(entity, factoryProvider) }
    }
}