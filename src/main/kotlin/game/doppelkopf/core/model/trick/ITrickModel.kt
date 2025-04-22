package game.doppelkopf.core.model.trick

import game.doppelkopf.core.model.IBaseModel
import game.doppelkopf.persistence.model.trick.TrickEntity

interface ITrickModel : ITrickProperties, IBaseModel<TrickEntity> {
}