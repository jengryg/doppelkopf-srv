package game.doppelkopf.core.model.user

import game.doppelkopf.common.IBaseModel
import game.doppelkopf.adapter.persistence.model.user.UserEntity

interface IUserModel : IUserProperties, IBaseModel<UserEntity> {
}