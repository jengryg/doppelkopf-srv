package game.doppelkopf.core.model.round

import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.core.cards.Deck
import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.game.IGameModel
import game.doppelkopf.core.model.hand.IHandModel
import game.doppelkopf.core.model.player.IPlayerModel
import game.doppelkopf.core.model.trick.ITrickModel
import game.doppelkopf.core.model.user.IUserModel

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

    override val tricks: Map<Int, ITrickModel>
        get() = entity.tricks.associate {
            it.number to factoryProvider.trick.create(it)
        }

    override val deck: Deck
        get() = Deck.create(deckMode)

    /**
     * Adds [h] to the [hands] of this round.
     */
    fun addHand(h: IHandModel) {
        entity.hands.add(h.entity)
    }

    fun getCurrentTrick(): ITrickModel? {
        return entity.tricks.maxByOrNull { it.number }?.let { factoryProvider.trick.create(it) }
    }
}