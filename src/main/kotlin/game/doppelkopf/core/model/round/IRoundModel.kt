package game.doppelkopf.core.model.round

import game.doppelkopf.core.common.IBaseModel
import game.doppelkopf.persistence.model.round.RoundEntity

interface IRoundModel : IRoundProperties, IBaseModel<RoundEntity> {
}