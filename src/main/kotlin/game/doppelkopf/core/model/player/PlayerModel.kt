package game.doppelkopf.core.model.player

import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.core.model.ModelFactoryProvider

class PlayerModel(
    entity: PlayerEntity,
    factoryProvider: ModelFactoryProvider
) : PlayerModelAbstract(entity, factoryProvider)