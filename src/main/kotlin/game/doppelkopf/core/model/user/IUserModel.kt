package game.doppelkopf.core.model.user

import game.doppelkopf.core.model.IBaseModel
import game.doppelkopf.persistence.model.user.UserEntity

interface IUserModel : IUserProperties, IBaseModel<UserEntity> {
}