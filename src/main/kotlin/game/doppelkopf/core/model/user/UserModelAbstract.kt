package game.doppelkopf.core.model.user

import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.adapter.persistence.model.user.UserEntity

abstract class UserModelAbstract(
    entity: UserEntity,
    factoryProvider: ModelFactoryProvider
) : IUserModel, IUserProperties by entity, ModelAbstract<UserEntity>(entity, factoryProvider)