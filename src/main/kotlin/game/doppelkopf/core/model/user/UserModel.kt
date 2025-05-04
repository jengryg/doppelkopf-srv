package game.doppelkopf.core.model.user

import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.core.model.ModelFactoryProvider

class UserModel(
    entity: UserEntity,
    factoryProvider: ModelFactoryProvider
) : UserModelAbstract(entity, factoryProvider)