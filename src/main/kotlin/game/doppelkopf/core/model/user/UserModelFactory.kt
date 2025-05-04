package game.doppelkopf.core.model.user

import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.common.IModelFactory
import game.doppelkopf.core.model.ModelFactoryCache
import game.doppelkopf.core.model.ModelFactoryProvider

class UserModelFactory(
    private val factoryProvider: ModelFactoryProvider
) : IModelFactory<UserEntity, UserModel> {
    private val cache = ModelFactoryCache<UserEntity, UserModel>()

    override fun create(entity: UserEntity): UserModel {
        return cache.getOrPut(entity) { UserModel(entity, factoryProvider) }
    }
}