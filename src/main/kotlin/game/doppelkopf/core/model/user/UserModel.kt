package game.doppelkopf.core.model.user

import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.adapter.persistence.model.user.UserEntity

class UserModel(
    entity: UserEntity,
    factoryProvider: ModelFactoryProvider
) : UserModelAbstract(entity, factoryProvider)