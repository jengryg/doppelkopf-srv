package game.doppelkopf.core.model.round

import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.game.IGameModel
import game.doppelkopf.core.model.hand.IHandModel
import game.doppelkopf.core.model.player.IPlayerModel
import game.doppelkopf.core.model.user.IUserModel
import game.doppelkopf.persistence.model.round.RoundEntity

abstract class RoundModelAbstract(
    entity: RoundEntity,
    factoryProvider: ModelFactoryProvider
) : IRoundModel, IRoundProperties by entity, ModelAbstract<RoundEntity>(entity, factoryProvider) {
    override val game: IGameModel
        get() = factoryProvider.game.create(entity.game)

    override val dealer: IPlayerModel
        get() = factoryProvider.player.create(entity.dealer)

    override val hands: Map<IUserModel, IHandModel>
        get() = entity.hands.associate {
            factoryProvider.user.create(it.player.user) to factoryProvider.hand.create(it)
        }

    /**
     * Adds [h] to the [hands] of this round.
     */
    fun addHand(h: IHandModel) {
        entity.hands.add(h.entity)
    }
}