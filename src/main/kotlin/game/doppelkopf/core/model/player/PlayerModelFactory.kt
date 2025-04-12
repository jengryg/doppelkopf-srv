package game.doppelkopf.core.model.player

import game.doppelkopf.core.model.IModelFactory
import game.doppelkopf.persistence.model.player.PlayerEntity
import org.springframework.stereotype.Service

@Service
class PlayerModelFactory : IModelFactory<PlayerEntity, PlayerModel> {
    override fun create(entity: PlayerEntity): PlayerModel {
        return PlayerModel(entity)
    }
}