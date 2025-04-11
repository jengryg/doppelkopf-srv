package game.doppelkopf.core.model.round

import game.doppelkopf.core.cards.Deck
import game.doppelkopf.core.model.game.GameModel
import game.doppelkopf.core.model.game.IGameModel
import game.doppelkopf.core.model.hand.HandModel
import game.doppelkopf.core.model.hand.IHandModel
import game.doppelkopf.core.model.player.IPlayerModel
import game.doppelkopf.core.model.player.PlayerModel
import game.doppelkopf.persistence.model.round.RoundEntity

class RoundModel(
    override val entity: RoundEntity
) : IRoundProperties by entity, IRoundModel {
    override val game: IGameModel
        get() = GameModel(entity.game)

    override val dealer: IPlayerModel
        get() = PlayerModel(entity.dealer)

    override val deck: Deck
        get() = Deck.create(entity.deckMode)

    override val hands: List<IHandModel>
        get() = entity.hands.map { HandModel(it) }
}