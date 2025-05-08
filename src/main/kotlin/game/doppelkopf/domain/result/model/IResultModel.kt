package game.doppelkopf.domain.result.model

import game.doppelkopf.adapter.persistence.model.result.ResultEntity
import game.doppelkopf.common.model.IBaseModel
import game.doppelkopf.domain.round.model.IRoundModel

interface IResultModel : IResultProperties, IBaseModel<ResultEntity> {
    val round: IRoundModel

    fun getTotalPoints() : Int
}