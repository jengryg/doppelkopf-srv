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
        return listOf(
            // basic points
            pointsForWinning,
            // lostScore points
            pointsLostScore90,
            pointsLostScore60,
            pointsLostScore30,
            pointsLostScore00,
            // basicCalls points
            pointsBasicCallsRe,
            pointsBasicCallsKo,
            // under calls points
            pointsUnderCallsRe90,
            pointsUnderCallsKo90,
            pointsUnderCallsRe60,
            pointsUnderCallsKo60,
            pointsUnderCallsRe30,
            pointsUnderCallsKo30,
            pointsUnderCallsRe00,
            pointsUnderCallsKo00,
            // beating calls of opposite party points
            pointsBeatingRe90,
            pointsBeatingKo90,
            pointsBeatingRe60,
            pointsBeatingKo60,
            pointsBeatingRe30,
            pointsBeatingKo30,
            pointsBeatingRe00,
            pointsBeatingKo00,
            // special points
            pointsForOpposition,
            pointsForDoppelkopf,
            pointsForCharly,
        ).sum()
    }
}