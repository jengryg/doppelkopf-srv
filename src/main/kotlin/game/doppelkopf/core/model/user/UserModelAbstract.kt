package game.doppelkopf.core.model.user

import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.core.model.ModelFactoryProvider

abstract class UserModelAbstract(
    entity: UserEntity,
    factoryProvider: ModelFactoryProvider
) : IUserModel, IUserProperties by entity, ModelAbstract<UserEntity>(entity, factoryProvider)