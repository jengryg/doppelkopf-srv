package game.doppelkopf.domain.user

import game.doppelkopf.adapter.persistence.model.user.UserEntity
import game.doppelkopf.common.IBaseModel

interface IUserModel : IUserProperties, IBaseModel<UserEntity>