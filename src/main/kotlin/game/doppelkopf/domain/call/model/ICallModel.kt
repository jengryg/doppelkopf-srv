package game.doppelkopf.domain.call.model

import game.doppelkopf.adapter.persistence.model.call.CallEntity
import game.doppelkopf.common.model.IBaseModel
import game.doppelkopf.domain.hand.model.IHandModel

interface ICallModel : ICallProperties, IBaseModel<CallEntity> {
    val hand: IHandModel
}