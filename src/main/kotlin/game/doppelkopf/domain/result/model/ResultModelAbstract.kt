package game.doppelkopf.domain.result.model

import game.doppelkopf.adapter.persistence.model.result.ResultEntity
import game.doppelkopf.domain.ModelAbstract
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.round.model.IRoundModel

abstract class ResultModelAbstract(
    entity: ResultEntity,
    factoryProvider: ModelFactoryProvider
) : IResultModel, IResultProperties by entity, ModelAbstract<ResultEntity>(entity, factoryProvider) {
    override val round: IRoundModel
        get() = factoryProvider.round.create(entity.round)

    override fun getTotalPoints(): Int {
        return pointsForWinning + pointsForOpposition +
                pointsForScore090 + pointsForScore060 + pointsForScore030 + pointsForScore000 +
                pointsForCharly +
                pointsForDoppelkopf
    }
}