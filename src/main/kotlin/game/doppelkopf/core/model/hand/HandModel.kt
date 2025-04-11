package game.doppelkopf.core.model.hand

import game.doppelkopf.core.cards.Card
import game.doppelkopf.core.model.player.IPlayerModel
import game.doppelkopf.core.model.player.PlayerModel
import game.doppelkopf.core.model.round.IRoundModel
import game.doppelkopf.core.model.round.RoundModel
import game.doppelkopf.persistence.model.hand.HandEntity

class HandModel(
    override val entity: HandEntity
) : IHandProperties by entity, IHandModel {
    override val round: IRoundModel
        get() = RoundModel(entity.round)

    override val player: IPlayerModel
        get() = PlayerModel(entity.player)

    override val cardsRemaining: List<Card>
        get() = round.deck.getCards(entity.cardsRemaining)

    override val cardsPlayed: List<Card>
        get() = round.deck.getCards(entity.cardsPlayed)

}