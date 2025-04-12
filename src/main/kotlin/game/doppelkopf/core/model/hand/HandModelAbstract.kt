package game.doppelkopf.core.model.hand

import game.doppelkopf.core.cards.Card
import game.doppelkopf.core.common.IBaseModel
import game.doppelkopf.core.model.player.PlayerModel
import game.doppelkopf.core.model.round.RoundModel
import game.doppelkopf.persistence.model.hand.HandEntity

/**
 * [HandModelAbstract] provides automatic delegation of [IHandProperties] and implements manual delegation of relations
 * of [HandEntity].
 */
abstract class HandModelAbstract(
    override val entity: HandEntity
) : IHandProperties by entity, IBaseModel<HandEntity> {
    val round: RoundModel
        get() = RoundModel(entity.round)

    val player: PlayerModel
        get() = PlayerModel(entity.player)

    val cardsRemaining: List<Card>
        get() = round.deck.getCards(entity.cardsRemaining)

    val cardsPlayed: List<Card>
        get() = round.deck.getCards(entity.cardsPlayed)
}