package game.doppelkopf.core.model.round

import game.doppelkopf.core.cards.Deck
import game.doppelkopf.core.model.game.GameModel
import game.doppelkopf.core.model.game.GameModelAbstract
import game.doppelkopf.core.model.hand.HandModel
import game.doppelkopf.core.model.hand.HandModelAbstract
import game.doppelkopf.core.model.player.PlayerModelAbstract
import game.doppelkopf.core.model.player.PlayerModel
import game.doppelkopf.persistence.model.round.RoundEntity

class RoundModel(
    entity: RoundEntity
) : RoundModelAbstract(entity) {

}