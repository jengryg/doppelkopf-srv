package game.doppelkopf.core.model.user

import game.doppelkopf.core.model.IModelFactory
import game.doppelkopf.persistence.model.user.UserEntity
import org.springframework.stereotype.Service

@Service
class UserModelFactory : IModelFactory<UserEntity, UserModel> {
    override fun create(entity: UserEntity): UserModel {
        return UserModel(entity)
    }
}