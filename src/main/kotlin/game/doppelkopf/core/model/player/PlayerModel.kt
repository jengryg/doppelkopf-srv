package game.doppelkopf.core.model.player

import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.adapter.persistence.model.player.PlayerEntity

class PlayerModel(
    entity: PlayerEntity,
    factoryProvider: ModelFactoryProvider
) : PlayerModelAbstract(entity, factoryProvider)