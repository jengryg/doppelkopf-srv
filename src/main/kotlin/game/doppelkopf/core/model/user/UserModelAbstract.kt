package game.doppelkopf.core.model.user

import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.core.model.game.GameModel
import game.doppelkopf.core.model.player.PlayerModel
import game.doppelkopf.persistence.model.user.UserEntity

/**
 * [UserModelAbstract] provides automatic delegation of [IUserProperties] and implements manuel delegations of
 * relations of [UserEntity].
 */
abstract class UserModelAbstract(
    entity: UserEntity
) : IUserProperties by entity, ModelAbstract<UserEntity>(entity) {
    val createdGames: List<GameModel>
        get() = entity.createdGames.map { GameModel(it) }

    val players: List<PlayerModel>
        get() = entity.players.map { PlayerModel(it) }
}