package game.doppelkopf.core.model.hand

import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.player.IPlayerModel
import game.doppelkopf.core.model.round.IRoundModel
import game.doppelkopf.persistence.model.hand.HandEntity

abstract class HandModelAbstract(
    entity: HandEntity,
    factoryProvider: ModelFactoryProvider
) : IHandModel, IHandProperties by entity, ModelAbstract<HandEntity>(entity, factoryProvider) {
    override val round: IRoundModel
        get() = factoryProvider.round.create(entity.round)

    override val player: IPlayerModel
        get() = factoryProvider.player.create(entity.player)
}