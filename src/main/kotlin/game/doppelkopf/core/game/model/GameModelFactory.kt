package game.doppelkopf.core.game.model

import game.doppelkopf.persistence.model.game.GameEntity
import org.springframework.stereotype.Service

@Service
class GameModelFactory {
    fun create(game: GameEntity): GameModel {
        return GameModel(game)
    }
}