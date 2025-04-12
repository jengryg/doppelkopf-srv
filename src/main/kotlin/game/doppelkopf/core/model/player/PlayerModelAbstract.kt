package game.doppelkopf.core.model.player

import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.core.model.game.GameModel
import game.doppelkopf.core.model.round.RoundModel
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.persistence.model.player.PlayerEntity

/**
 * [PlayerModelAbstract] provides automatic delegation of [IPlayerProperties] and implements manual delegation of
 * relations of [PlayerEntity].
 */
abstract class PlayerModelAbstract(
    entity: PlayerEntity
) : IPlayerProperties by entity, ModelAbstract<PlayerEntity>(entity) {
    val game: GameModel
        get() = GameModel(entity.game)

    val user: UserModel
        get() = UserModel(entity.user)

    val dealtRounds: List<RoundModel>
        get() = entity.dealtRounds.map { RoundModel(it) }
}