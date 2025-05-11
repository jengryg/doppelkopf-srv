package game.doppelkopf.domain.round.model

import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.domain.ModelAbstract
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.deck.model.Deck
import game.doppelkopf.domain.game.model.IGameModel
import game.doppelkopf.domain.hand.model.IHandModel
import game.doppelkopf.domain.player.model.IPlayerModel
import game.doppelkopf.domain.result.model.IResultModel
import game.doppelkopf.domain.trick.model.ITrickModel
import game.doppelkopf.domain.turn.model.ITurnModel
import game.doppelkopf.domain.user.model.IUserModel
import game.doppelkopf.utils.Teamed

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

    override val turns: Map<Int, ITurnModel>
        get() = entity.turns.associate {
            it.number to factoryProvider.turn.create(it)
        }

    override val results: Teamed<IResultModel>?
        get() = Teamed.from(entity.results) { it.team.internal }
            ?.map { factoryProvider.result.create(it) }

    override val deck: Deck
        get() = Deck.create(deckMode)

    /**
     * Adds [model] to the [hands] of this round.
     */
    override fun addHand(model: IHandModel) {
        entity.hands.add(model.entity)
    }

    /**
     * Adds [model] to the [tricks] of this round.
     */
    override fun addTrick(model: ITrickModel) {
        entity.tricks.add(model.entity)
    }

    /**
     * Adds [model] to the [turns] of this round.
     */
    override fun addTurn(model: ITurnModel) {
        entity.turns.add(model.entity)
    }

    /**
     * Adds [model] to the [results] of this round.
     */
    override fun addResult(model: IResultModel) {
        entity.results.add(model.entity)
    }

    fun getCurrentTrick(): ITrickModel? {
        return entity.tricks.maxByOrNull { it.number }?.let { factoryProvider.trick.create(it) }
    }
}