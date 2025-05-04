package game.doppelkopf.domain

import game.doppelkopf.domain.game.model.GameModelFactory
import game.doppelkopf.domain.hand.model.HandModelFactory
import game.doppelkopf.domain.player.model.PlayerModelFactory
import game.doppelkopf.domain.round.model.RoundModelFactory
import game.doppelkopf.domain.trick.model.TrickModelFactory
import game.doppelkopf.domain.turn.model.TurnModelFactory
import game.doppelkopf.domain.user.UserModelFactory

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
    val trick = TrickModelFactory(this)
    val turn = TurnModelFactory(this)
}