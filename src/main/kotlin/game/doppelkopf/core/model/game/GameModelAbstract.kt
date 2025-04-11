package game.doppelkopf.core.model.game

import game.doppelkopf.core.common.IBaseModel
import game.doppelkopf.core.model.player.PlayerModel
import game.doppelkopf.core.model.round.RoundModel
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.persistence.model.game.GameEntity

/**
 * [GameModelAbstract] provides automatic delegation of [IGameProperties] and implements manual delegation of relations
 * of [GameEntity].
 */
abstract class GameModelAbstract(
    override val entity: GameEntity
) : IGameProperties by entity, IBaseModel<GameEntity> {
    val creator: UserModel
        get() = UserModel(entity.creator)

    val players: List<PlayerModel>
        get() = entity.players.map { PlayerModel(it) }

    fun addPlayer(player: PlayerModel) {
        entity.players.add(player.entity)
    }

    val rounds: List<RoundModel>
        get() = entity.rounds.map { RoundModel(it) }

    fun addRound(round: RoundModel) {
        entity.rounds.add(round.entity)
    }
}