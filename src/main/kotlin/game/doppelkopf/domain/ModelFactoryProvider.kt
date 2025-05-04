package game.doppelkopf.domain

import game.doppelkopf.domain.game.service.GameModelFactory
import game.doppelkopf.domain.hand.service.HandModelFactory
import game.doppelkopf.domain.player.service.PlayerModelFactory
import game.doppelkopf.domain.round.service.RoundModelFactory
import game.doppelkopf.domain.trick.service.TrickModelFactory
import game.doppelkopf.domain.turn.service.TurnModelFactory
import game.doppelkopf.domain.user.service.UserModelFactory

/**
 * Initialize the [ModelFactoryProvider] and supply it to the first model that is instantiated.
 * The model implementations will pass the reference to the [ModelFactoryProvider] forward.
 * This resolver allows us to build up the model graph including their relations reusing instances of the basic models
 * that are created by the [game.doppelkopf.common.service.IModelFactory] implementations referenced in this provider.
 */
class ModelFactoryProvider {
    val user = UserModelFactory(this)
    val game = GameModelFactory(this)
    val player = PlayerModelFactory(this)
    val round = RoundModelFactory(this)
    val hand = HandModelFactory(this)
    val trick = TrickModelFactory(this)
    val turn = TurnModelFactory(this)
}