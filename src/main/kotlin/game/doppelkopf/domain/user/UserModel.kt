package game.doppelkopf.domain.user

import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.domain.ModelFactoryProvider

class UserModel(
    entity: UserEntity,
    factoryProvider: ModelFactoryProvider
) : UserModelAbstract(entity, factoryProvider)