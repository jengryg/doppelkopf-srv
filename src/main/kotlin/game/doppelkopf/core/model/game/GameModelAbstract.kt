package game.doppelkopf.core.model.game

import game.doppelkopf.core.errors.ofGameFailed
import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.player.IPlayerModel
import game.doppelkopf.core.model.round.IRoundModel
import game.doppelkopf.core.model.user.IUserModel
import game.doppelkopf.adapter.persistence.model.game.GameEntity

abstract class GameModelAbstract(
    entity: GameEntity,
    factoryProvider: ModelFactoryProvider
) : IGameModel, IGameProperties by entity, ModelAbstract<GameEntity>(entity, factoryProvider) {
    override val creator: IUserModel
        get() = factoryProvider.user.create(entity.creator)

    override val players: Map<IUserModel, IPlayerModel>
        get() = entity.players.associate {
            factoryProvider.user.create(it.user) to factoryProvider.player.create(it)
        }

    override val rounds: Map<Int, IRoundModel>
        get() = entity.rounds.associate {
            it.number to factoryProvider.round.create(it)
        }

    /**
     * Adds [p] to the [players] of this game.
     */
    fun addPlayer(p: IPlayerModel) {
        entity.players.add(p.entity)
    }

    /**
     * Adds [r] to the [rounds] of this game.
     */
    fun addRound(r: IRoundModel) {
        entity.rounds.add(r.entity)
    }

    /**
     * Determine the current dealer of the game using the [IPlayerModel.dealer] flag.
     */
    fun getCurrentDealer(): Result<IPlayerModel> {
        return players.values.singleOrNull { it.dealer }?.let { Result.success(it) }
            ?: Result.ofGameFailed("Could not uniquely determine the current dealer.", entity.id)
    }
}