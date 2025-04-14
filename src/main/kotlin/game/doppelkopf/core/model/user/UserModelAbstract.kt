package game.doppelkopf.core.model.user

import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.persistence.model.user.UserEntity

/**
 * [UserModelAbstract] provides automatic delegation of [IUserProperties] and implements manuel delegations of
 * relations of [UserEntity].
 */
abstract class UserModelAbstract(
    entity: UserEntity
) : IUserProperties by entity, ModelAbstract<UserEntity>(entity)