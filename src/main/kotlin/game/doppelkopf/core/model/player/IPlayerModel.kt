package game.doppelkopf.core.model.player

import game.doppelkopf.core.common.IBaseModel
import game.doppelkopf.persistence.model.player.PlayerEntity

interface IPlayerModel : IPlayerProperties, IBaseModel<PlayerEntity> {
}