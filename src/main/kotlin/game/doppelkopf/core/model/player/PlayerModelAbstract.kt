package game.doppelkopf.core.model.player

import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.core.model.ModelFactoryProvider
import game.doppelkopf.core.model.game.IGameModel
import game.doppelkopf.core.model.user.IUserModel
import game.doppelkopf.persistence.model.player.PlayerEntity

abstract class PlayerModelAbstract(
    entity: PlayerEntity,
    factoryProvider: ModelFactoryProvider
) : IPlayerModel, IPlayerProperties by entity, ModelAbstract<PlayerEntity>(entity, factoryProvider) {
    override val user: IUserModel
        get() = factoryProvider.user.create(entity.user)

    override val game: IGameModel
        get() = factoryProvider.game.create(entity.game)
}