package game.doppelkopf.domain.user.model

import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.domain.ModelAbstract
import game.doppelkopf.domain.ModelFactoryProvider

abstract class UserModelAbstract(
    entity: UserEntity,
    factoryProvider: ModelFactoryProvider
) : IUserModel, IUserProperties by entity, ModelAbstract<UserEntity>(entity, factoryProvider)