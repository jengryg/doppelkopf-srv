package game.doppelkopf.domain.hand.model

import game.doppelkopf.adapter.persistence.model.hand.HandEntity
import game.doppelkopf.common.model.IBaseModel
import game.doppelkopf.domain.call.enums.CallType
import game.doppelkopf.domain.call.model.ICallModel
import game.doppelkopf.domain.deck.model.Card
import game.doppelkopf.domain.hand.enums.Team
import game.doppelkopf.domain.player.model.IPlayerModel
import game.doppelkopf.domain.round.model.IRoundModel

interface IHandModel : IHandProperties, IBaseModel<HandEntity> {
    val round: IRoundModel
    val player: IPlayerModel
    val cards: List<Card>
    val size: Int
    val playedCardCount: Int
    val calls: Map<CallType, ICallModel>

    fun addCall(model: ICallModel)

    fun assignPublicTeam(team: Team)
    fun assignPrivateTeam(team: Team)
    fun revealTeam()
}