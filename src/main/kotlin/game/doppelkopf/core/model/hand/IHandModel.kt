package game.doppelkopf.core.model.hand

import game.doppelkopf.core.common.IBaseModel
import game.doppelkopf.persistence.model.hand.HandEntity

interface IHandModel : IHandProperties, IBaseModel<HandEntity> {
}