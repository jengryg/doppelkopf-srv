package game.doppelkopf.core.model.turn

import game.doppelkopf.core.model.IBaseModel
import game.doppelkopf.persistence.model.turn.TurnEntity

interface ITurnModel : ITurnProperties, IBaseModel<TurnEntity> {
}