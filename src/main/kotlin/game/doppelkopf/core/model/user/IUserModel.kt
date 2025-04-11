package game.doppelkopf.core.model.user

import game.doppelkopf.core.common.IBaseModel
import game.doppelkopf.persistence.model.user.UserEntity

interface IUserModel : IUserProperties, IBaseModel<UserEntity> {
}