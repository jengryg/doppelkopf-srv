package game.doppelkopf.core.model.user

import game.doppelkopf.core.model.IModelFactory
import game.doppelkopf.persistence.model.user.UserEntity
import java.util.*

class UserModel private constructor(
    entity: UserEntity
) : UserModelAbstract(entity) {
    companion object : IModelFactory<UserEntity, UserModel> {
        private val instances = mutableMapOf<UUID, UserModel>()

        override fun create(entity: UserEntity): UserModel {
            return instances.getOrPut(entity.id) { UserModel(entity) }
        }
    }
}