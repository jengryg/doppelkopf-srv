package game.doppelkopf.domain.hand.model

import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.common.IBaseModel
import game.doppelkopf.core.cards.Card
import game.doppelkopf.core.common.enums.Team
import game.doppelkopf.domain.player.model.IPlayerModel
import game.doppelkopf.domain.round.model.IRoundModel

interface IHandModel : IHandProperties, IBaseModel<HandEntity> {
    val round: IRoundModel
    val player: IPlayerModel
    val cards: List<Card>
    val size: Int

    fun assignPublicTeam(team: Team)
    fun assignPrivateTeam(team: Team)
}