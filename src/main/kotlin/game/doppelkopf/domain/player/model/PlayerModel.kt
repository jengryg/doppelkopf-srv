package game.doppelkopf.domain.player.model

import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.core.model.ModelFactoryProvider

class PlayerModel(
    entity: PlayerEntity,
    factoryProvider: ModelFactoryProvider
) : PlayerModelAbstract(entity, factoryProvider)