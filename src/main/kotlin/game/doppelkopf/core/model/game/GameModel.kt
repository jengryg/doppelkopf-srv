package game.doppelkopf.core.model.game

import game.doppelkopf.core.model.player.IPlayerModel
import game.doppelkopf.core.model.player.PlayerModel
import game.doppelkopf.core.model.round.IRoundModel
import game.doppelkopf.core.model.round.RoundModel
import game.doppelkopf.core.model.user.IUserModel
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.persistence.model.game.GameEntity

class GameModel(
    override val entity: GameEntity
) : IGameProperties by entity, IGameModel {
    override val creator: IUserModel
        get() = UserModel(entity.creator)

    override val players: List<IPlayerModel>
        get() = entity.players.map { PlayerModel(it) }

    override fun addPlayer(player: IPlayerModel) {
        entity.players.add(player.entity)
    }

    override val rounds: List<IRoundModel>
        get() = entity.rounds.map { RoundModel(it) }

    override fun addRound(round: IRoundModel) {
        entity.rounds.add(round.entity)
    }
}