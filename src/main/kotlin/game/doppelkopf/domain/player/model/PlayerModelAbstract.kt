package game.doppelkopf.domain.player.model

import game.doppelkopf.adapter.persistence.model.player.PlayerEntity
import game.doppelkopf.domain.ModelAbstract
import game.doppelkopf.domain.ModelFactoryProvider
import game.doppelkopf.domain.game.model.IGameModel
import game.doppelkopf.domain.user.model.IUserModel

abstract class PlayerModelAbstract(
    entity: PlayerEntity,
    factoryProvider: ModelFactoryProvider
) : IPlayerModel, IPlayerProperties by entity, ModelAbstract<PlayerEntity>(entity, factoryProvider) {
    override val user: IUserModel
        get() = factoryProvider.user.create(entity.user)

    override val game: IGameModel
        get() = factoryProvider.game.create(entity.game)
}