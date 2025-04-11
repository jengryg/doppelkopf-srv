package game.doppelkopf.core.model.player

import game.doppelkopf.core.common.IBaseModel
import game.doppelkopf.core.model.game.GameModel
import game.doppelkopf.core.model.game.GameModelAbstract
import game.doppelkopf.core.model.round.RoundModel
import game.doppelkopf.core.model.round.RoundModelAbstract
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.core.model.user.UserModelAbstract
import game.doppelkopf.persistence.model.player.PlayerEntity

/**
 * [PlayerModelAbstract] provides automatic delegation of [IPlayerProperties] and implements manual delegation of
 * relations of [PlayerEntity].
 */
abstract class PlayerModelAbstract(
    override val entity: PlayerEntity
) : IPlayerProperties by entity, IBaseModel<PlayerEntity> {
    val game: GameModelAbstract
        get() = GameModel(entity.game)

    val user: UserModelAbstract
        get() = UserModel(entity.user)

    val dealtRounds: List<RoundModelAbstract>
        get() = entity.dealtRounds.map { RoundModel(it) }
}