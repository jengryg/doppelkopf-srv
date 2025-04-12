package game.doppelkopf.core.model.round

import game.doppelkopf.core.cards.Deck
import game.doppelkopf.core.common.IBaseModel
import game.doppelkopf.core.model.game.GameModel
import game.doppelkopf.core.model.hand.HandModel
import game.doppelkopf.core.model.player.PlayerModel
import game.doppelkopf.persistence.model.round.RoundEntity

/**
 * [RoundModelAbstract] provides automatic delegation of [IRoundProperties] and implements manual delegation of
 * relations of [RoundEntity].
 */
abstract class RoundModelAbstract(
    override val entity: RoundEntity
) : IRoundProperties by entity, IBaseModel<RoundEntity> {
    val game: GameModel
        get() = GameModel(entity.game)

    val dealer: PlayerModel
        get() = PlayerModel(entity.dealer)

    val deck: Deck
        get() = Deck.create(entity.deckMode)

    val hands: List<HandModel>
        get() = entity.hands.map { HandModel(it) }

    fun addHand(hand: HandModel) {
        if (entity.hands.contains(hand.entity)) {
            return
        }
        entity.hands.add(hand.entity)
    }
}