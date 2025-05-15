package game.doppelkopf.domain.round.model

import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.common.errors.GameFailedException
import game.doppelkopf.domain.ModelAbstract
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.call.model.ICallModel
import game.doppelkopf.domain.deck.model.Deck
import game.doppelkopf.domain.game.model.IGameModel
import game.doppelkopf.domain.hand.model.IHandModel
import game.doppelkopf.domain.player.model.IPlayerModel
import game.doppelkopf.domain.result.model.IResultModel
import game.doppelkopf.domain.round.enums.RoundContract
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

    override fun getCurrentTrick(): ITrickModel? {
        return entity.tricks.maxByOrNull { it.number }?.let { factoryProvider.trick.create(it) }
    }

    override fun getCalls(): Teamed<List<ICallModel>> {
        val teamedHands = Teamed.filter(hands.values) { it.internalTeam }
        return teamedHands.map { list -> list.flatMap { it.calls.values }.sortedBy { it.callType.orderIndex } }
    }

    override fun determineCardCountOffset(): Int {
        if (contract != RoundContract.MARRIAGE_SOLO && contract != RoundContract.MARRIAGE_RESOLVED) {
            // offset is only applicable to marriages that got resolved during play.
            return 0
        }

        // we already established that the marriage was resolved, thus we expect that there is a resolving trick
        val resolvingTrick = tricks.values.singleOrNull { it.resolvedMarriage }
            ?: throw GameFailedException(
                "Could not determine the trick that resolved the marriage of round $this.",
                entity.id
            )

        // By rules, the offset is one less than the resolving trick number (1,2 or 3).
        return (resolvingTrick.number - 1)
    }
}