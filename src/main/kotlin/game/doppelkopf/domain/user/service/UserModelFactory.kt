package game.doppelkopf.domain.user.service

import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.common.IModelFactory
import game.doppelkopf.common.ModelFactoryCache
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.user.model.UserModel

class UserModelFactory(
    private val factoryProvider: ModelFactoryProvider
) : IModelFactory<UserEntity, UserModel> {
    private val cache = ModelFactoryCache<UserEntity, UserModel>()

    override fun create(entity: UserEntity): UserModel {
        return cache.getOrPut(entity) { UserModel(entity, factoryProvider) }
    }
}