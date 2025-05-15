package game.doppelkopf.domain.round.model

import game.doppelkopf.adapter.persistence.model.round.RoundEntity
import game.doppelkopf.common.model.IBaseModel
import game.doppelkopf.domain.call.model.ICallModel
import game.doppelkopf.domain.deck.model.Deck
import game.doppelkopf.domain.game.model.IGameModel
import game.doppelkopf.domain.hand.model.IHandModel
import game.doppelkopf.domain.player.model.IPlayerModel
import game.doppelkopf.domain.result.model.IResultModel
import game.doppelkopf.domain.trick.model.ITrickModel
import game.doppelkopf.domain.turn.model.ITurnModel
import game.doppelkopf.domain.user.model.IUserModel
import game.doppelkopf.utils.Teamed

interface IRoundModel : IRoundProperties, IBaseModel<RoundEntity> {
    val game: IGameModel
    val dealer: IPlayerModel
    val hands: Map<IUserModel, IHandModel>
    val deck: Deck
    val tricks: Map<Int, ITrickModel>
    val turns: Map<Int, ITurnModel>
    val results: Teamed<IResultModel>?

    fun addHand(model: IHandModel)
    fun addTrick(model: ITrickModel)
    fun addTurn(model: ITurnModel)
    fun addResult(model: IResultModel)

    fun getCurrentTrick(): ITrickModel?
    fun getCalls() : Teamed<List<ICallModel>>
    fun determineCardCountOffset(): Int
}