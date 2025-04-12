package game.doppelkopf.core.model.game

import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.core.model.player.PlayerModel
import game.doppelkopf.core.model.round.RoundModel
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.persistence.model.game.GameEntity

/**
 * [GameModelAbstract] provides automatic delegation of [IGameProperties] and implements manual delegation of relations
 * of [GameEntity].
 */
abstract class GameModelAbstract(
    entity: GameEntity
) : IGameProperties by entity, ModelAbstract<GameEntity>(entity) {
    val creator: UserModel
        get() = UserModel(entity.creator)

    val players: List<PlayerModel>
        get() = entity.players.map { PlayerModel(it) }

    fun addPlayer(player: PlayerModel) {
        if (entity.players.contains(player.entity)) {
            return
        }
        entity.players.add(player.entity)
    }

    fun getCurrentDealer(): PlayerModel {
        return players.single { it.dealer }
    }

    val rounds: List<RoundModel>
        get() = entity.rounds.map { RoundModel(it) }

    fun addRound(round: RoundModel) {
        if (entity.rounds.contains(round.entity)) {
            return
        }
        entity.rounds.add(round.entity)
    }
}