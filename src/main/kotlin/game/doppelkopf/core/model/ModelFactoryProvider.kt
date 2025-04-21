package game.doppelkopf.core.model

import game.doppelkopf.core.model.game.GameModelFactory
import game.doppelkopf.core.model.hand.HandModelFactory
import game.doppelkopf.core.model.player.PlayerModelFactory
import game.doppelkopf.core.model.round.RoundModelFactory
import game.doppelkopf.core.model.user.UserModelFactory

/**
 * Initialize the [ModelFactoryProvider] and supply it to the first model that is instantiated.
 * The model implementations will pass the reference to the [ModelFactoryProvider] forward.
 */
class ModelFactoryProvider {
    val user = UserModelFactory(this)
    val game = GameModelFactory(this)
    val player = PlayerModelFactory(this)
    val round = RoundModelFactory(this)
    val hand = HandModelFactory(this)
}