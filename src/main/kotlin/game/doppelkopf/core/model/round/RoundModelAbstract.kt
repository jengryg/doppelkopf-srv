package game.doppelkopf.core.model.round

import game.doppelkopf.core.model.ModelAbstract
import game.doppelkopf.core.model.game.GameModel
import game.doppelkopf.core.model.hand.HandModel
import game.doppelkopf.core.model.player.PlayerModel
import game.doppelkopf.core.model.user.UserModel
import game.doppelkopf.persistence.model.round.RoundEntity

/**
 * [RoundModelAbstract] provides automatic delegation of [IRoundProperties] and demands implementation of relevant
 * related models.
 */
abstract class RoundModelAbstract(
    entity: RoundEntity
) : IRoundProperties by entity, ModelAbstract<RoundEntity>(entity) {
    val game get() = GameModel.create(entity.game)

    val dealer get() = PlayerModel.create(entity.dealer)

    val hands
        get() = entity.hands.associate {
            UserModel.create(it.player.user) to HandModel.create(it)
        }

    fun addHand(h: HandModel) {
        entity.hands.add(h.entity)
    }

    /**
     * Determines the hand of the player that sits behind the player of the given [h].
     *
     * @return the hand directly behind [h].
     */
    fun getHandBehind(h: HandModel): HandModel {
        val index = h.index % 4
        return hands.values.singleOrNull { it.index == index }
            ?: throw GameFailedException("could not determine the hand behind $h.")
    }
}