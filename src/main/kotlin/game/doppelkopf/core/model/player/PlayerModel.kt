package game.doppelkopf.core.model.player

import game.doppelkopf.core.model.game.GameModel
import game.doppelkopf.core.model.game.IGameModel
import game.doppelkopf.core.model.round.IRoundModel
import game.doppelkopf.core.model.round.RoundModel
import game.doppelkopf.core.model.user.IUserModel
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.persistence.model.player.PlayerEntity

class PlayerModel(
    override val entity: PlayerEntity
) : IPlayerProperties by entity, IPlayerModel {
    override val game: IGameModel
        get() = GameModel(entity.game)

    override val user: IUserModel
        get() = UserModel(entity.user)

    override val dealtRounds: List<IRoundModel>
        get() = entity.dealtRounds.map { RoundModel(it) }
}