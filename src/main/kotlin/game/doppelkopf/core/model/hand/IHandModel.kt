package game.doppelkopf.core.model.hand

import game.doppelkopf.core.model.IBaseModel
import game.doppelkopf.core.model.player.IPlayerModel
import game.doppelkopf.core.model.round.IRoundModel
import game.doppelkopf.persistence.model.hand.HandEntity

interface IHandModel : IHandProperties, IBaseModel<HandEntity> {
    val round: IRoundModel
    val player: IPlayerModel
}